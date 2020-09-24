package com.mairo.cataclysm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbit")
public class RabbitProps {

  private RabbitGlobalConfig global;
  private RabbitQueueConfig listPlayersQueue;
  private RabbitQueueConfig addPlayerQueue;
  private RabbitQueueConfig errorsQueue;


  @Data
  public static class RabbitGlobalConfig {

    private String username;
    private String password;
    private String host;
    private String virtualHost;
    private int port;
    private String exchange;
  }


  @Data
  public static class RabbitQueueConfig {

    private String name;
    private String key;
  }
}
