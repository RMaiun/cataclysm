package com.mairo.cataclysm.service;

import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.delegate.StatsServiceDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final RoundsService roundsService;
  private final StatsServiceDelegate statsServiceHelper;

  public Mono<SeasonStatsRows> seasonStatisticsRows(String seasonName) {
    return roundsService.findAllRounds(seasonName)
        .map(statsServiceHelper::prepareSeasonStatsTable);
  }

  public Mono<SeasonShortStats> seasonShortInfoStatistics(String seasonName) {
    return roundsService.findAllRounds(seasonName)
        .map(rounds -> statsServiceHelper.prepareSeasonShortStats(seasonName, rounds));
  }
}
