package com.mairo.cataclysm.service;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.dto.FoundAllPlayers;
import com.mairo.cataclysm.core.dto.FullRound;
import com.mairo.cataclysm.core.dto.PlayerRank;
import com.mairo.cataclysm.core.dto.PlayerStats;
import com.mairo.cataclysm.core.dto.SeasonNotificationData;
import com.mairo.cataclysm.core.dto.SeasonShortStats;
import com.mairo.cataclysm.core.properties.AppProps;
import com.mairo.cataclysm.core.service.PlayerService;
import com.mairo.cataclysm.core.service.RoundsService;
import com.mairo.cataclysm.core.service.SeasonService;
import com.mairo.cataclysm.core.service.StatisticsService;
import com.mairo.cataclysm.core.utils.DateUtils;
import com.mairo.cataclysm.core.utils.IdGenerator;
import com.mairo.cataclysm.core.utils.SeasonUtils;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.utils.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Component
@RequiredArgsConstructor
public class SeasonStatsSender {

  public static final Logger logger = LogManager.getLogger(SeasonStatsSender.class);

  private final AppProps appProps;
  private final PlayerService playerService;
  private final RabbitSender rabbitSender;
  private final StatisticsService statisticsService;
  private final RoundsService roundsService;
  private final SeasonService seasonService;


  public Mono<Void> sendFinalSeasonStats() {
    return Mono.just(appProps.isNotificationsEnabled())
        .doOnNext(__ -> logger.info("Starting Final Season Stats Reports generation with notificationEnabled {}", appProps.isNotificationsEnabled()))
        .filter(it -> it)
        .then(Mono.just(ZonedDateTime.now(ZoneId.of(appProps.getReportTimezone()))))
        .doOnNext(date -> logger.info("Check that {} equals to last day of current season where time = 20:00", date))
        .flatMap(this::shouldSend)
        .filter(SeasonNotificationData::isReadyToBeProcessed)
        .doOnNext(snd -> logger.info("Final Season Stats Reports generation criteria were passed successfully for season {}", snd.getSeason()))
        .flatMap(snd -> findPlayersWithRanks(snd.getSeason()))
        .doOnNext(ranks -> logger.info("{} users will receive final stats notification", ranks.size()))
        .flatMapMany(Flux::fromIterable)
        .flatMap(this::sendNotificationForPlayer)
        .then(seasonService.ackSendFinalNotifications());
  }

  private Mono<SeasonNotificationData> shouldSend(ZonedDateTime currentDateTime) {
    return seasonService.findSeasonWithoutNotifications()
        .map(maybeSeason ->
            maybeSeason.map(foundSeason ->
                new SeasonNotificationData(foundSeason.getName(), SeasonUtils.firstBeforeSecond(foundSeason.getName(), SeasonUtils.currentSeason()) && DateUtils.notLateToSend(currentDateTime)))
                .orElse(new SeasonNotificationData(null, false)))
        .doOnNext(snd -> logger.info("Current date {} criteria for sending notifications", snd.isReadyToBeProcessed() ? "passed" : "didn't pass"));

  }

  private Mono<List<PlayerRank>> findPlayersWithRanks(String season) {
    return Mono.zip(
        statisticsService.seasonShortInfoStatistics(season),
        playerService.findAllPlayers(),
        roundsService.findAllRounds(season))
        .map(this::preparePlayerRanks);
  }

  private List<PlayerRank> preparePlayerRanks(Tuple3<SeasonShortStats, FoundAllPlayers, List<FullRound>> data) {
    List<String> participatedSurnames = data.getT3().stream()
        .flatMap(fr -> Stream.of(fr.getWinner1(), fr.getWinner2(), fr.getLoser1(), fr.getLoser2()))
        .distinct()
        .collect(Collectors.toList());

    Map<String, Player> allPlayersBySurname = data.getT2().getPlayers().stream()
        .collect(Collectors.toMap(Player::getSurname, Function.identity()));

    List<String> playersWithRating = data.getT1().getPlayersRating().stream()
        .map(PlayerStats::getSurname)
        .collect(Collectors.toList());

    Map<String, PlayerStats> statsPerPlayer = data.getT1().getPlayersRating().stream()
        .collect(Collectors.toMap(PlayerStats::getSurname, Function.identity()));

    return participatedSurnames.stream()
        .map(allPlayersBySurname::get)
        .filter(p -> p.isNotificationsEnabled() && nonNull(p.getTid()))
        .map(p -> {
          Optional<PlayerStats> playerStats = Optional.ofNullable(statsPerPlayer.get(p.getSurname()));
          String score = playerStats.map(PlayerStats::getScore).orElse("");
          int gamesPlayed = (int) data.getT3().stream()
              .filter(r -> asList(r.getWinner1(), r.getWinner2(), r.getLoser1(), r.getLoser2()).contains(p.getSurname()))
              .count();
          int rank = playersWithRating.indexOf(p.getSurname());
          return new PlayerRank(p.getSurname(), p.getTid(), rank + 1, score, gamesPlayed, data.getT3().size(), participatedSurnames.size());
        })
        .collect(Collectors.toList());
  }

  private Mono<OutputMessage> sendNotificationForPlayer(PlayerRank playerRank) {
    StringBuilder builder = messageBuilder(playerRank);
    String msg = playerRank.getRank() > 0
        ? messageForPlayerWithDefinedRating(builder, playerRank)
        : messageForPlayerWithoutRating(builder, playerRank);
    logger.info(OutputMessage.ok(playerRank.getTid(), IdGenerator.msgId(), msg));
    return rabbitSender.send(OutputMessage.ok(playerRank.getTid(), IdGenerator.msgId(), msg));
  }

  private StringBuilder messageBuilder(PlayerRank rank) {
    return new StringBuilder()
        .append(Constants.PREFIX)
        .append(String.format("Season %s is successfully closed.", SeasonUtils.currentSeason()))
        .append(Constants.LINE_SEPARATOR)
        .append(String.format("%d players played %d games in total.", rank.getAllPlayers(), rank.getAllGames()))
        .append(Constants.LINE_SEPARATOR);
  }

  private String messageForPlayerWithDefinedRating(StringBuilder builder, PlayerRank rank) {
    String winRate = new BigDecimal(rank.getScore()).multiply(BigDecimal.valueOf(100L)).setScale(1, RoundingMode.HALF_EVEN).toString();
    return builder
        .append("Your achievements:")
        .append(Constants.LINE_SEPARATOR)
        .append(String.format("- #%d in rating", rank.getRank()))
        .append(Constants.LINE_SEPARATOR)
        .append(String.format("- win rate %s%%", winRate))
        .append(Constants.LINE_SEPARATOR)
        .append(String.format("- games played: %d", rank.getGamesPlayed()))
        .append(Constants.LINE_SEPARATOR)
        .append("\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D")
        .append(Constants.SUFFIX)
        .toString();
  }

  private String messageForPlayerWithoutRating(StringBuilder builder, PlayerRank rank) {
    return builder
        .append(String.format("You've played %d games in this season.", rank.getGamesPlayed()))
        .append(Constants.LINE_SEPARATOR)
        .append(String.format("Unfortunately you must play %d games", appProps.getExpectedGames()))
        .append(Constants.LINE_SEPARATOR)
        .append("to be included into rating.")
        .append(Constants.LINE_SEPARATOR)
        .append("Hope that in next season")
        .append(Constants.LINE_SEPARATOR)
        .append("you will reach our game limit")
        .append(Constants.LINE_SEPARATOR)
        .append("and will show us your best.")
        .append(Constants.LINE_SEPARATOR)
        .append("⭐⭐⭐")
        .append(Constants.SUFFIX)
        .toString();
  }
}
