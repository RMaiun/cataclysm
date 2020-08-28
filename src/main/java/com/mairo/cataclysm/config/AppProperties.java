package com.mairo.cataclysm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private int topPlayersLimit;
  private int minGames;
  private int winPoints;
  private int winShutoutPoints;
  private int losePoints;
  private int loseShutoutPoints;
}
