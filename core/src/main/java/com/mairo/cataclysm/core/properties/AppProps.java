package com.mairo.cataclysm.core.properties;

import com.mairo.cataclysm.core.dto.AlgorithmType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProps {

  private int topPlayersLimit;
  private int winPoints;
  private int winShutoutPoints;
  private int losePoints;
  private int loseShutoutPoints;
  private AlgorithmType algorithm;
  private String archiveReceiver;
  private boolean notificationsEnabled;
  private int expectedGames;
  private String reportTimezone;
  private String privileged;
}
