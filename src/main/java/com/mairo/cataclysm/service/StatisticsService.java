package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.validation.ValidationTypes.seasonValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.helper.StatsServiceHelper;
import com.mairo.cataclysm.model.RoundsModel;
import com.mairo.cataclysm.properties.AppProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final RoundsModel roundsModel;
  private final StatsServiceHelper statsServiceHelper;
  private final AppProps appProperties;

  public Mono<SeasonStatsRows> seasonStatisticsRows(String seasonName) {
    return validate(seasonName, seasonValidationType)
        .then(roundsModel.findAllRounds(seasonName))
        .map(statsServiceHelper::prepareSeasonStatsTable);
  }

  public Mono<SeasonShortStats> seasonShortInfoStatistics(String seasonName) {
    return validate(seasonName, seasonValidationType)
        .then(roundsModel.findAllRounds(seasonName))
        .map(rounds -> statsServiceHelper.prepareSeasonShortStats(seasonName, rounds, appProperties.getAlgorithm()));
  }
}
