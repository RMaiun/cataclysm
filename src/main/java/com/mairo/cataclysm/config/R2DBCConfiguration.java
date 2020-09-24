package com.mairo.cataclysm.config;

import com.mairo.cataclysm.config.properties.DbProps;
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import dev.miku.r2dbc.mysql.constant.ZeroDateOption;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories
@RequiredArgsConstructor
class R2DBCConfiguration extends AbstractR2dbcConfiguration {

  private final DbProps dbProps;

  @Bean
  @Override
  public ConnectionFactory connectionFactory() {
    MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
        .host(dbProps.getHost())
        .port(dbProps.getPort()) // optional, default 3306
        .database(dbProps.getDatabase()) // optional, default null, null means not specifying the database
        .username(dbProps.getUsername())
        .password(dbProps.getPassword()) // optional, default null, null means has no password
        .connectTimeout(Duration.ofSeconds(3)) // optional, default null, null means no timeout
        .zeroDateOption(ZeroDateOption.USE_NULL) // optional, default ZeroDateOption.USE_NULL
        .useServerPrepareStatement() // Use server-preparing statements, default use client-preparing statements
        .build();
    return MySqlConnectionFactory.from(configuration);
  }


}
