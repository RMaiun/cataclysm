package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Player;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        .all()
        .collectList()
        .map(list -> list.stream()
            .sorted(Comparator.comparing(Player::getId))
            .collect(Collectors.toList()));
  }
}
