package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.mairo.cataclysm.validation.ValidationTypes.seasonValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

@RestController
@RequestMapping("stats")
@RequiredArgsConstructor
public class StatsController {

  private final StatisticsService statisticsService;

  @GetMapping("/table/{season}")
  public Mono<SeasonStatsRows> seasonRowsStatistic(@PathVariable String season) {
    return validate(season, seasonValidationType)
        .flatMap(statisticsService::seasonStatisticsRows);
  }

  @GetMapping("/short/{season}")
  public Mono<SeasonShortStats> generalSeasonStatistics(@PathVariable String season) {
    return validate(season, seasonValidationType)
        .flatMap(statisticsService::seasonShortInfoStatistics);
  }
}
