package com.mairo.cataclysm.core.service;

import com.mairo.cataclysm.core.domain.Season;
import com.mairo.cataclysm.core.exception.SeasonNotFoundException;
import com.mairo.cataclysm.core.repository.SeasonRepository;
import com.mairo.cataclysm.core.utils.DateUtils;
import com.mairo.cataclysm.core.utils.SeasonUtils;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SeasonService {

  public static final Logger logger = LogManager.getLogger(SeasonService.class);

  private final SeasonRepository seasonRepository;

  public Mono<Season> findSeason(String season) {
    return seasonRepository.getSeason(season);
  }

  public Mono<Season> findSeasonSafely(String season) {
    return seasonRepository.getSeason(season)
        .doOnNext(s -> logger.info("Season {} is already found in system with id {}", season, s.getId()))
        .onErrorResume(err -> prepareAbsentSeason(season));
  }

  public Mono<Optional<Season>> findSeasonWithoutNotifications() {
    return seasonRepository.findFirstSeasonWithoutNotification();
  }

  public Mono<Void> ackSendFinalNotifications() {
    return seasonRepository.findFirstSeasonWithoutNotification()
        .flatMap(maybeSeason -> maybeSeason
            .map(s -> seasonRepository.updateSeason(new Season(s.getId(), s.getName(), DateUtils.now()))
                .then())
            .orElse(Mono.empty()));
  }

  public Mono<Season> prepareAbsentSeason(String expected) {
    logger.info("Prepare absent season where current: {} and expected: {}", SeasonUtils.currentSeason(), expected);
    if (SeasonUtils.currentSeason().equals(expected)) {
      logger.info("Creating new season {}", expected);
      return seasonRepository.saveSeason(Season.of(expected));
    } else {
      logger.warn("Expected season ({}) is not the current one ({})", expected, SeasonUtils.currentSeason());
      return Mono.error(new SeasonNotFoundException(expected));
    }
  }
}
