package com.mairo.cataclysm.helper;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.mairo.cataclysm.config.AppProperties;
import com.mairo.cataclysm.dto.AlgorithmType;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerStats;
import com.mairo.cataclysm.dto.RatingWithGames;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.dto.StatsCalcData;
import com.mairo.cataclysm.dto.Streak;
import com.mairo.cataclysm.utils.DateUtils;
import com.mairo.cataclysm.utils.SeasonUtils;
import io.vavr.Tuple4;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsServiceHelper {

  private final AppProperties appProperties;

  public SeasonShortStats prepareSeasonShortStats(String seasonName, List<FullRound> rounds, AlgorithmType algorithmType) {
    SeasonShortStats stats = new SeasonShortStats();
    stats.setGamesPlayed(rounds.size());
    stats.setSeason(seasonName);

    Pair<LocalDate, LocalDate> seasonGate = SeasonUtils.seasonGate(seasonName);
    LocalDate now = LocalDate.now();
    if (now.compareTo(seasonGate.getRight()) > 0) {
      stats.setDaysToSeasonEnd(0);
    } else {
      int daysLeft = seasonGate.getRight().getDayOfYear() - now.getDayOfYear();
      stats.setDaysToSeasonEnd(daysLeft);
    }
    List<PlayerStats> topPlayers = AlgorithmType.PERCENTAGE == algorithmType
        ? percentagePlayersStats(rounds)
        : pointsPlayersStats(rounds);
    stats.setPlayersRating(topPlayers);

    Pair<Optional<Streak>, Optional<Streak>> optionalOptionalPair = calculateStreaks(rounds);
    optionalOptionalPair.getLeft().ifPresent(stats::setBestStreak);
    optionalOptionalPair.getRight().ifPresent(stats::setWorstStreak);

    return stats;
  }

  private List<PlayerStats> percentagePlayersStats(List<FullRound> rounds) {
    Map<String, Integer> allRounds = prepareAcceptedPlayersForShortStats(aggregatePlayersWithGames(rounds), true);
    Map<String, BigDecimal> winRounds = new HashMap<>();
    rounds.forEach(r -> {
      fillWinMap(r.getWinner1(), winRounds);
      fillWinMap(r.getWinner2(), winRounds);
    });
    return allRounds.entrySet().stream()
        .map(e -> new PlayerStats(e.getKey(), preparePercentageStats(e, winRounds), e.getValue()))
        .sorted(Comparator.comparing(ps -> new BigDecimal(ps.getScore()), Comparator.reverseOrder()))
        .collect(toList());
  }

  private String preparePercentageStats(Map.Entry<String, Integer> entry, Map<String, BigDecimal> winRounds) {
    BigDecimal foundWins = winRounds.getOrDefault(entry.getKey(), BigDecimal.ZERO);
    BigDecimal result = foundWins.divide(BigDecimal.valueOf(entry.getValue()), new MathContext(3, RoundingMode.HALF_EVEN));

    BigDecimal roundedResult = result.setScale(3, RoundingMode.UNNECESSARY);
    return roundedResult.toString();
  }

  private void fillWinMap(String surname, Map<String, BigDecimal> winRounds) {
    BigDecimal winFound = winRounds.getOrDefault(surname, BigDecimal.ZERO);
    winRounds.put(surname, winFound.add(BigDecimal.ONE));
  }

  private List<PlayerStats> pointsPlayersStats(List<FullRound> rounds) {
    return calculatePointsForPlayers(rounds, true)
        .stream()
        .sorted(Comparator.comparing(RatingWithGames::getRating, Comparator.reverseOrder()))
        .map(rwg -> new PlayerStats(rwg.getPlayer(), String.valueOf(rwg.getRating()), rwg.getGames()))
        .collect(toList());
  }

  private Pair<Optional<Streak>, Optional<Streak>> calculateStreaks(List<FullRound> rounds) {
    Map<String, Tuple4<Integer, Integer, Integer, Integer>> results = rounds.stream()
        .flatMap(r -> Stream.of(r.getWinner1(), r.getWinner2(), r.getLoser1(), r.getLoser2()))
        .distinct()
        .collect(Collectors.toMap(x -> x, x -> new Tuple4<>(0, 0, 0, 0)));

    rounds.forEach(r -> {
      checkStreak(results, r.getWinner1(), 1);
      checkStreak(results, r.getWinner2(), 1);
      checkStreak(results, r.getLoser1(), -1);
      checkStreak(results, r.getLoser2(), -1);
    });

    Optional<Streak> best = results.entrySet().stream()
        .map(e -> new Streak(e.getKey(), e.getValue()._2))
        .max(Comparator.comparing(Streak::getGames));

    Optional<Streak> worst = results.entrySet().stream()
        .map(e -> new Streak(e.getKey(), e.getValue()._4))
        .max(Comparator.comparing(Streak::getGames));
    return Pair.of(best, worst);
  }

  private void checkStreak(Map<String, Tuple4<Integer, Integer, Integer, Integer>> results,
      String surname, int score) {
    Tuple4<Integer, Integer, Integer, Integer> found = results.get(surname);
    if (score > 0) {
      int currentWin = found._1 + 1;
      int maxWin = currentWin > found._2 ? currentWin : found._2;
      results.put(surname, new Tuple4<>(currentWin, maxWin, 0, found._4));
    } else {
      int currentLose = found._3 + 1;
      int maxLose = currentLose > found._4 ? currentLose : found._4;
      results.put(surname, new Tuple4<>(0, found._2, currentLose, maxLose));
    }
  }

  public SeasonStatsRows prepareSeasonStatsTable(List<FullRound> rounds) {
    List<RatingWithGames> playerStats = calculatePointsForPlayers(rounds, false);

    List<String> headers = playerStats.stream()
        .sorted(Comparator.comparing(RatingWithGames::getPid))
        .map(RatingWithGames::getPlayer)
        .collect(toList());

    List<Integer> totals = playerStats
        .stream()
        .sorted(Comparator.comparing(RatingWithGames::getPid))
        .map(RatingWithGames::getRating)
        .collect(toList());

    List<List<String>> games = rounds.stream()
        .map(r -> transformRoundIntoRow(r, headers))
        .collect(toList());

    List<String> createdDates = rounds.stream()
        .map(FullRound::getCreated)
        .map(DateUtils::formatDateWithHour)
        .collect(toList());

    return new SeasonStatsRows(headers, totals, games, createdDates, rounds.size());
  }

  private List<RatingWithGames> calculatePointsForPlayers(List<FullRound> rounds, boolean shortStats) {
    List<StatsCalcData> roundData = aggregatePlayersWithGames(rounds);
    Map<String, Integer> acceptedPlayers = prepareAcceptedPlayersForShortStats(roundData, shortStats);

    Map<String, List<StatsCalcData>> dataByPlayer = roundData.stream()
        .filter(t -> acceptedPlayers.containsKey(t.getPlayer()))
        .collect(groupingBy(StatsCalcData::getPlayer));

    return prepareRatingWithGames(dataByPlayer);
  }

  private List<StatsCalcData> aggregatePlayersWithGames(List<FullRound> rounds) {
    return rounds.stream()
        .flatMap(r -> Stream.of(
            new StatsCalcData(r.getW1Id(), r.getWinner1(), winPoints(r.isShutout())),
            new StatsCalcData(r.getW2Id(), r.getWinner2(), winPoints(r.isShutout())),
            new StatsCalcData(r.getL1Id(), r.getLoser1(), losePoints(r.isShutout())),
            new StatsCalcData(r.getL2Id(), r.getLoser2(), losePoints(r.isShutout()))))
        .collect(toList());
  }

  private Map<String, Integer> prepareAcceptedPlayersForShortStats(List<StatsCalcData> roundData, boolean filterByGames) {
    Stream<Entry<String, Integer>> stream = roundData.stream()
        .collect(groupingBy(StatsCalcData::getPlayer, mapping(StatsCalcData::getQty, reducing(0, Integer::sum))))
        .entrySet().stream();
    if (filterByGames) {
      return stream.filter(e -> e.getValue() >= 30)
          .collect(toMap(Entry::getKey, Entry::getValue));
    } else {
      return stream.collect(toMap(Entry::getKey, Entry::getValue));
    }
  }

  private List<RatingWithGames> prepareRatingWithGames(Map<String, List<StatsCalcData>> dataByPlayer) {
    return dataByPlayer.entrySet().stream()
        .map(this::ratingWithGames)
        .collect(toList());
  }

  private RatingWithGames ratingWithGames(Entry<String, List<StatsCalcData>> e) {
    long id = e.getValue().stream().findFirst().map(StatsCalcData::getPid).orElse(-1L);
    int points = e.getValue().stream().map(StatsCalcData::getPoints).reduce(1000, Integer::sum);
    int games = e.getValue().size();
    return new RatingWithGames(id, e.getKey(), points, games);
  }

  private List<String> transformRoundIntoRow(FullRound r, List<String> headers) {
    List<String> row = IntStream.range(0, headers.size()).mapToObj(x -> "").collect(toList());
    String winPoints = String.valueOf(winPoints(r.isShutout()));
    String losePoints = String.valueOf(losePoints(r.isShutout()));
    row.set(headers.indexOf(r.getWinner1()), winPoints);
    row.set(headers.indexOf(r.getWinner2()), winPoints);
    row.set(headers.indexOf(r.getLoser1()), losePoints);
    row.set(headers.indexOf(r.getLoser2()), losePoints);
    return row;
  }

  private int winPoints(boolean shutout) {
    return calculatePoints(true, shutout);
  }

  private int losePoints(boolean shutout) {
    return calculatePoints(false, shutout);
  }

  private int calculatePoints(boolean win, boolean shutout) {
    if (win && shutout) {
      return appProperties.getWinShutoutPoints();
    } else if (win) {
      return appProperties.getWinPoints();
    } else if (shutout) {
      return appProperties.getLoseShutoutPoints();
    } else {
      return appProperties.getLosePoints();
    }
  }
}
