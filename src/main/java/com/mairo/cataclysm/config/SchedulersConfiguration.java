package com.mairo.cataclysm.config;

import com.mairo.cataclysm.schedulers.SeasonStatsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SchedulersConfiguration {

  private final SeasonStatsSender seasonStatsSender;

  private static boolean sendOnce = false;

  @Scheduled(cron = "0 0 20 * * ?")
  public void finalSeasonReportNotifications() {
    seasonStatsSender.sendFinalSeasonStats(false).subscribe();
  }

  @Scheduled(cron = "0 0/1 * * * ?")
  public void finalSeasonReportNotificationsOnce() {
    if (!sendOnce) {
      sendOnce = true;
      System.out.println("Once");
      seasonStatsSender.sendFinalSeasonStats(true).subscribe();
    }
  }
}
