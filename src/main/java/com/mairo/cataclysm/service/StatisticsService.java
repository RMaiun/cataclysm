package com.mairo.cataclysm.service;

import com.mairo.cataclysm.config.AppProperties;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.helper.StatsServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final RoundsService roundsService;
  private final StatsServiceHelper statsServiceHelper;
  private final AppProperties appProperties;

  public Mono<SeasonStatsRows> seasonStatisticsRows(String seasonName) {
    return roundsService.findAllRounds(seasonName)
        .map(statsServiceHelper::prepareSeasonStatsTable);
  }

  public Mono<SeasonShortStats> seasonShortInfoStatistics(String seasonName) {
    return roundsService.findAllRounds(seasonName)
        .map(rounds -> statsServiceHelper.prepareSeasonShortStats(seasonName, rounds, appProperties.getAlgorithm()));
  }
}
