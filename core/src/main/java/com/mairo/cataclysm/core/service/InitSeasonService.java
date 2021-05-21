package com.mairo.cataclysm.core.service;


import com.mairo.cataclysm.core.domain.Season;
import com.mairo.cataclysm.core.utils.SeasonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InitSeasonService {

  private final SeasonService seasonService;

  public Mono<Season> initSeason() {
    return seasonService.findSeasonSafely(SeasonUtils.currentSeason());
  }
}
