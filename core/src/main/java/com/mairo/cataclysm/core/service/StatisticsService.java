package com.mairo.cataclysm.core.service;

import com.mairo.cataclysm.core.dto.SeasonShortStats;
import com.mairo.cataclysm.core.dto.SeasonStatsRows;
import com.mairo.cataclysm.core.helper.StatsServiceHelper;
import com.mairo.cataclysm.core.properties.AppProps;
import com.mairo.cataclysm.core.validation.ValidationTypes;
import com.mairo.cataclysm.core.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final RoundsService roundsService;
  private final StatsServiceHelper statsServiceHelper;
  private final AppProps appProperties;

  public Mono<SeasonStatsRows> seasonStatisticsRows(String seasonName) {
    return Validator.validate(seasonName, ValidationTypes.seasonValidationType)
        .then(roundsService.findAllRounds(seasonName))
        .map(statsServiceHelper::prepareSeasonStatsTable);
  }

  public Mono<SeasonShortStats> seasonShortInfoStatistics(String seasonName) {
    return Validator.validate(seasonName, ValidationTypes.seasonValidationType)
        .then(roundsService.findAllRounds(seasonName))
        .map(rounds -> statsServiceHelper.prepareSeasonShortStats(seasonName, rounds, appProperties.getAlgorithm()));
  }
}
