package com.mairo.cataclysm.config;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.MAX_SIZE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import com.mairo.cataclysm.properties.DbProps;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
@RequiredArgsConstructor
class R2DBCConfiguration {

  private final DbProps dbProps;

  @Bean
  @Primary
  public ConnectionFactory connectionFactory() {
    ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(DRIVER, "pool")
        .option(PROTOCOL, "mysql") // driver identifier, PROTOCOL is delegated as DRIVER by the pool.
        .option(HOST, dbProps.getHost())
        .option(PORT, dbProps.getPort())
        .option(USER, dbProps.getUsername())
        .option(PASSWORD, dbProps.getPassword())
        .option(DATABASE, dbProps.getDatabase())
        .option(MAX_SIZE, 1000)
        .build());

    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(Duration.ofMinutes(30))
        .initialSize(50)
        .maxSize(200)
        .maxCreateConnectionTime(Duration.ofSeconds(3))
        .acquireRetry(3)
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean
  @Primary
  DatabaseClient dbc() {
    return DatabaseClient.create(connectionFactory());
  }
}
