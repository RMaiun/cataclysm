package com.mairo.cataclysm.config;

import com.mairo.cataclysm.service.SeasonStatsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SchedulersConfiguration {

  private final SeasonStatsSender seasonStatsSender;

  @Scheduled(cron = "0 0 20 * * ?")
  public void finalSeasonReportNotifications() {
    seasonStatsSender.sendFinalSeasonStats().subscribe();
  }

}
