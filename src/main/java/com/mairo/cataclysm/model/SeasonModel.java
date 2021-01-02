package com.mairo.cataclysm.model;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.exception.SeasonNotFoundException;
import com.mairo.cataclysm.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SeasonModel {

  public static final Logger logger = LogManager.getLogger(SeasonModel.class);

  private SeasonRepository seasonRepository;

  public Mono<Season> findSeason(String season) {
    return seasonRepository.getSeason(season);
  }

  public Mono<Season> findSeasonSafely(String season) {
    return seasonRepository.getSeason(season)
        .doOnNext(s -> logger.info("Season {} is already found in system with id {}", season, s.getId()))
        .onErrorResume(err -> prepareAbsentSeason(season));
  }

  public Mono<Season> prepareAbsentSeason(String expected) {
    logger.info("Prepare absent season where current: {} and expected: {}", currentSeason(), expected);
    if (currentSeason().equals(expected)) {
      logger.info("Creating new season {}", expected);
      return seasonRepository.findLastId()
          .map(id -> new Season(id + 1, expected))
          .flatMap(season -> seasonRepository.saveSeason(season).map(__ -> season));
    } else {
      logger.warn("Expected season ({}) is not the current one ({})", expected, currentSeason());
      return Mono.error(new SeasonNotFoundException(expected));
    }
  }
}
