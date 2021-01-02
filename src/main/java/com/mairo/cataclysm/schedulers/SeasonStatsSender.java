package com.mairo.cataclysm.schedulers;

import static com.mairo.cataclysm.processor.CommandProcessor.LINE_SEPARATOR;
import static com.mairo.cataclysm.processor.CommandProcessor.PREFIX;
import static com.mairo.cataclysm.processor.CommandProcessor.SUFFIX;
import static com.mairo.cataclysm.utils.IdGenerator.msgId;
import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.PlayerRank;
import com.mairo.cataclysm.dto.PlayerStats;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.properties.AppProps;
import com.mairo.cataclysm.rabbit.RabbitSender;
import com.mairo.cataclysm.service.PlayerService;
import com.mairo.cataclysm.service.RoundsService;
import com.mairo.cataclysm.service.StatisticsService;
import com.mairo.cataclysm.utils.SeasonUtils;
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


  public Flux<OutputMessage> sendFinalSeasonStats(boolean once) {
    return Mono.just(appProps.isNotificationsEnabled())
        .doOnNext(__ -> logger.info("Starting Final Season Stats Reports generation with notificationEnabled {}", appProps.isNotificationsEnabled()))
        .filter(it -> it)
        .then(Mono.just(ZonedDateTime.now(ZoneId.of(appProps.getReportTimezone()))))
        .doOnNext(date -> logger.info("Check that {} equals to last day of current season where time = 20:00", date))
        .filter(once ? x -> true : this::isTimingCorrect)
        .doOnNext(__ -> logger.info("Final Season Stats Reports generation criteria were passed successfully"))
        .flatMap(__ -> findPlayersWithRanks())
        .doOnNext(ranks -> logger.info("{} users will receive final stats notification", ranks.size()))
        .flatMapMany(Flux::fromIterable)
        .flatMap(this::sendNotificationForPlayer);
  }

  private boolean isTimingCorrect(ZonedDateTime currentDateTime) {
    int expectedDay = SeasonUtils.seasonGate(currentSeason()).getRight().getDayOfYear();
    int currentDay = currentDateTime.getDayOfYear();
    return currentDay == expectedDay && currentDateTime.getHour() == 20;
  }

  private Mono<List<PlayerRank>> findPlayersWithRanks() {
    return Mono.zip(
        statisticsService.seasonShortInfoStatistics("S4|2020"),
        playerService.findAllPlayers(),
        roundsService.findAllRounds("S4|2020"))
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
          return new PlayerRank(p.getSurname(), p.getId(), p.getTid(), rank + 1, score, gamesPlayed, data.getT3().size(), participatedSurnames.size());
        })
        .collect(Collectors.toList());
  }

  private Mono<OutputMessage> sendNotificationForPlayer(PlayerRank playerRank) {
    StringBuilder builder = messageBuilder(playerRank);
    String msg = playerRank.getRank() > 0
        ? messageForPlayerWithDefinedRating(builder, playerRank)
        : messageForPlayerWithoutRating(builder, playerRank);

    return rabbitSender.send(OutputMessage.ok(playerRank.getTid(), msgId(), msg));
  }

  private StringBuilder messageBuilder(PlayerRank rank) {
    return new StringBuilder()
        .append(PREFIX)
        .append(String.format("Season %s is successfully closed.", "S4|2020")) //todo
        .append(LINE_SEPARATOR)
        .append(String.format("%d players played %d games in total.", rank.getAllPlayers(), rank.getAllGames()))
        .append(LINE_SEPARATOR);
  }

  private String messageForPlayerWithDefinedRating(StringBuilder builder, PlayerRank rank) {
    String winRate = new BigDecimal(rank.getScore()).multiply(BigDecimal.valueOf(100L)).setScale(1, RoundingMode.HALF_EVEN).toString();
    return builder
        .append("Your achievements:")
        .append(LINE_SEPARATOR)
        .append(String.format("- #%d in rating", rank.getRank()))
        .append(LINE_SEPARATOR)
        .append(String.format("- win rate %s%%", winRate))
        .append(LINE_SEPARATOR)
        .append(String.format("- games played: %d", rank.getGamesPlayed()))
        .append(LINE_SEPARATOR)
        .append("\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D")
        .append(SUFFIX)
        .toString();
  }

  private String messageForPlayerWithoutRating(StringBuilder builder, PlayerRank rank) {
    return builder
        .append(String.format("You've played %d games in this season.", rank.getGamesPlayed()))
        .append(LINE_SEPARATOR)
        .append(String.format("Unfortunately you must play %d games", appProps.getExpectedGames()))
        .append(LINE_SEPARATOR)
        .append("to be included into rating.")
        .append(LINE_SEPARATOR)
        .append("Hope that in next season")
        .append(LINE_SEPARATOR)
        .append("you will reach our game limit")
        .append(LINE_SEPARATOR)
        .append("and will show us your best.")
        .append(LINE_SEPARATOR)
        .append("⭐⭐⭐")
        .append(SUFFIX)
        .toString();
  }
}
