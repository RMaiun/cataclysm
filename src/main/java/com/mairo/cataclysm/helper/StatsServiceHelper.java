package com.mairo.cataclysm.helper;

import com.mairo.cataclysm.config.AppProperties;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerStats;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class StatsServiceHelper {

  private final AppProperties appProperties;

  public SeasonShortStats prepareSeasonShortStats(String seasonName, List<FullRound> rounds) {
    SeasonShortStats stats = new SeasonShortStats();
    stats.setGamesPlayed(rounds.size());
    stats.setSeason(seasonName);
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    LocalDateTime firstDayOfQuarter = now.with(now.getMonth().firstMonthOfQuarter())
        .with(TemporalAdjusters.firstDayOfMonth());
    LocalDateTime lastDayOfQuarter = firstDayOfQuarter.plusMonths(2)
        .with(TemporalAdjusters.lastDayOfMonth());
    int daysLeft = lastDayOfQuarter.getDayOfYear() - now.getDayOfYear();
    stats.setDaysToSeasonEnd(daysLeft);

    List<PlayerStats> topPlayers = calculatePointsForPlayers(rounds).entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .limit(appProperties.topPlayersLimit)
        .map(e -> new PlayerStats(e.getKey(), e.getValue()))
        .collect(toList());
    stats.setTopPlayers(topPlayers);
    return stats;
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
        .map(r -> transformRoundIntoRow(r, headers)).collect(toList());

    return new SeasonStatsRows(headers, totals, games, rounds.size());
  }

  private Map<String, Integer> calculatePointsForPlayers(List<FullRound> rounds) {
    return rounds.stream()
        .flatMap(r -> Stream.of(
            Pair.of(r.getWinner1(), r.isShutout() ? appProperties.winShutoutPoints : appProperties.winPoints),
            Pair.of(r.getWinner2(), r.isShutout() ? appProperties.winShutoutPoints : appProperties.winPoints),
            Pair.of(r.getLoser1(), r.isShutout() ? appProperties.loseShutoutPoints : appProperties.losePoints),
            Pair.of(r.getLoser2(), r.isShutout() ? appProperties.loseShutoutPoints : appProperties.losePoints)))
        .collect(groupingBy(Pair::getKey, mapping(Pair::getRight, reducing(1000, Integer::sum))));
  }

  private List<String> transformRoundIntoRow(FullRound r, List<String> headers) {
    List<String> row = IntStream.range(0, headers.size()).mapToObj(x -> "").collect(toList());
    String winPoints = r.isShutout() ? String.valueOf(appProperties.winShutoutPoints) : String.valueOf(appProperties.winPoints);
    String losePoints = r.isShutout() ? String.valueOf(appProperties.loseShutoutPoints) : String.valueOf(appProperties.losePoints);
    row.set(headers.indexOf(r.getWinner1()), winPoints);
    row.set(headers.indexOf(r.getWinner2()), winPoints);
    row.set(headers.indexOf(r.getLoser1()), losePoints);
    row.set(headers.indexOf(r.getLoser2()), losePoints);
    return row;
  }
}
