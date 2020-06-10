package com.mairo.cataclysm.config;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import dev.miku.r2dbc.mysql.constant.ZeroDateOption;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories
class R2DBCConfiguration extends AbstractR2dbcConfiguration {
  @Bean
  public ConnectionFactory connectionFactory() {
    MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
        .host("127.0.0.1")
        .port(3306) // optional, default 3306
        .database("ufstats") // optional, default null, null means not specifying the database
        .username("root")
        .password("Mairo@888") // optional, default null, null means has no password
        .connectTimeout(Duration.ofSeconds(3)) // optional, default null, null means no timeout
        .zeroDateOption(ZeroDateOption.USE_NULL) // optional, default ZeroDateOption.USE_NULL
        .useServerPrepareStatement() // Use server-preparing statements, default use client-preparing statements
        .build();
    return MySqlConnectionFactory.from(configuration);
  }
}
