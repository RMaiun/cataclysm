package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.model.SeasonModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InitSeasonService {

  private final SeasonModel seasonModel;

  public Mono<Season> initSeason() {
    return seasonModel.findSeasonSafely(currentSeason());
  }
}
