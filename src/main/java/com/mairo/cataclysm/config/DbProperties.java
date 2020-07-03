package com.mairo.cataclysm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "db")
public class DbProperties {

  private String host;
  private int port;
  private String database;
  private String username;
  private String password;
}
