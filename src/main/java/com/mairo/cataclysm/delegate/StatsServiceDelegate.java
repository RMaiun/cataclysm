package com.mairo.cataclysm.delegate;

import com.mairo.cataclysm.config.AppProperties;
import com.mairo.cataclysm.dto.*;
import com.mairo.cataclysm.utils.DateUtils;
import com.mairo.cataclysm.utils.SeasonUtils;
import io.vavr.Tuple4;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class StatsServiceDelegate {

  private final AppProperties appProperties;

  public SeasonShortStats prepareSeasonShortStats(String seasonName, List<FullRound> rounds) {
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
    List<PlayerStats> topPlayers = calculatePointsForPlayers(rounds).entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(e -> new PlayerStats(e.getKey(), e.getValue()))
        .collect(toList());
    stats.setPlayersRating(topPlayers);

    Pair<Optional<Streak>, Optional<Streak>> optionalOptionalPair = calculateStreaks(rounds);
    optionalOptionalPair.getLeft().ifPresent(stats::setBestStreak);
    optionalOptionalPair.getRight().ifPresent(stats::setWorstStreak);

    return stats;
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
    List<String> headers = rounds.stream()
        .flatMap(r -> Stream.of(r.getWinner1(), r.getWinner2(), r.getLoser1(), r.getLoser2()))
        .distinct()
        .sorted()
        .collect(toList());

    List<Integer> totals = calculatePointsForPlayers(rounds)
        .entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
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

  private Map<String, Integer> calculatePointsForPlayers(List<FullRound> rounds) {
    return rounds.stream()
        .flatMap(r -> Stream.of(
            Pair.of(r.getWinner1(), r.isShutout() ? appProperties.getWinShutoutPoints() : appProperties.getWinPoints()),
            Pair.of(r.getWinner2(), r.isShutout() ? appProperties.getWinShutoutPoints() : appProperties.getWinPoints()),
            Pair.of(r.getLoser1(), r.isShutout() ? appProperties.getLoseShutoutPoints() : appProperties.getLosePoints()),
            Pair.of(r.getLoser2(), r.isShutout() ? appProperties.getLoseShutoutPoints() : appProperties.getLosePoints())))
        .collect(groupingBy(Pair::getKey, mapping(Pair::getRight, reducing(1000, Integer::sum))));
  }

  private List<String> transformRoundIntoRow(FullRound r, List<String> headers) {
    List<String> row = IntStream.range(0, headers.size()).mapToObj(x -> "").collect(toList());
    String winPoints = r.isShutout() ? String.valueOf(appProperties.getWinShutoutPoints()) : String.valueOf(appProperties.getWinPoints());
    String losePoints = r.isShutout() ? String.valueOf(appProperties.getLoseShutoutPoints()) : String.valueOf(appProperties.getLosePoints());
    row.set(headers.indexOf(r.getWinner1()), winPoints);
    row.set(headers.indexOf(r.getWinner2()), winPoints);
    row.set(headers.indexOf(r.getLoser1()), losePoints);
    row.set(headers.indexOf(r.getLoser2()), losePoints);
    return row;
  }
}
