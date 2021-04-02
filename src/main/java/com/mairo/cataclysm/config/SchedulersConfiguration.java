package com.mairo.cataclysm.config;

import com.mairo.cataclysm.service.InitSeasonService;
import com.mairo.cataclysm.service.SeasonStatsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SchedulersConfiguration {

  private final SeasonStatsSender seasonStatsSender;
  private final InitSeasonService initSeasonService;

  @Scheduled(cron = "0 0 * * * ?")
  public void finalSeasonReportNotifications() {
    seasonStatsSender.sendFinalSeasonStats().subscribe();
  }

  @Scheduled(cron = "0 0 1 * * ?")
  public void initSeason() {
    initSeasonService.initSeason().subscribe();
  }
}
