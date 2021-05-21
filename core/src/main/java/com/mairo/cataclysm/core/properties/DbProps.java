package com.mairo.cataclysm.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "db")
public class DbProps {

  private String host;
  private int port;
  private String database = "cata";
  private String username;
  private String password;
  private boolean recreateMongoIndexes;
  private String mongoUrl = "mongodb://root:password@localhost:27017/cata";

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isRecreateMongoIndexes() {
    return recreateMongoIndexes;
  }

  public void setRecreateMongoIndexes(boolean recreateMongoIndexes) {
    this.recreateMongoIndexes = recreateMongoIndexes;
  }

  public String getMongoUrl() {
    return mongoUrl;
  }

  public void setMongoUrl(String mongoUrl) {
    this.mongoUrl = mongoUrl;
  }
}
