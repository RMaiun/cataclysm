package com.mairo.cataclysm.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "db")
public class DbProps {

  private String host;
  private int port;
  private String database;
  private String username;
  private String password;
  private boolean recreateMongoIndexes;
  private String mongoUrl;
}
