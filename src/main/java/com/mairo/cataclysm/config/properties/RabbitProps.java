package com.mairo.cataclysm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rabbit")
public class RabbitProps {

  private String username;
  private String password;
  private String host;
  private String virtualHost;
  private int port;
  private String inputQueue;
  private String outputQueue;
  private String errorQueue;
  private String binaryQueue;
}
