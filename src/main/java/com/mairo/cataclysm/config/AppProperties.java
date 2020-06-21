package com.mairo.cataclysm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@EnableConfigurationProperties
public class AppProperties {

  private int topPlayersLimit;
  private int winPoints;
  private int winShutoutPoints;
  private int losePoints;
  private int loseShutoutPoints;
}
