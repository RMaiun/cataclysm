package com.mairo.cataclysm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CataclysmApplication {

  public static void main(String[] args) {
    SpringApplication.run(CataclysmApplication.class, args);
  }

}
