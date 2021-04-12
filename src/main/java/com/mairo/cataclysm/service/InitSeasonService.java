package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;

import com.mairo.cataclysm.domain.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InitSeasonService {

  private final SeasonService seasonService;

  public Mono<Season> initSeason() {
    return seasonService.findSeasonSafely(currentSeason());
  }
}
