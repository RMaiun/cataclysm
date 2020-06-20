package com.mairo.cataclysm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "db")
@Getter
@Setter
@EnableConfigurationProperties
public class DbProperties {

  private String host;
  private int port;
  private String database;
  private String username;
  private String password;
}
