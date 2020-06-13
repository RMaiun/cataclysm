package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Player;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PlayerRepository {

  private final DatabaseClient dbClient;

  public PlayerRepository(DatabaseClient dbClient) {
    this.dbClient = dbClient;
  }

  public Mono<List<Player>> listAll() {
    return dbClient.select()
        .from(Player.class)
        .as(Player.class)
        .all().collectList();
  }
}
