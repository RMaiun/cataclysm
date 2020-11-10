package com.mairo.cataclysm.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mairo.cataclysm.domain.Player;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Criteria;
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

  public Mono<List<Player>> findPlayers(List<String> surnames) {
    return dbClient.select()
        .from(Player.class)
        .matching(where("surname").in(surnames))
        .as(Player.class)
        .all().collectList();
  }

  public Mono<Optional<Player>> getPlayer(String name) {
    return dbClient.select()
        .from(Player.class)
        .matching(where("surname").is(name))
        .as(Player.class)
        .one()
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()));
  }

  public Mono<Optional<Player>> getPlayerByCriteria(Criteria criteria) {
    return dbClient.select()
        .from(Player.class)
        .matching(criteria)
        .as(Player.class)
        .one()
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()));
  }

  public Mono<Long> savePlayer(Player player) {
    return dbClient.insert()
        .into(Player.class)
        .using(player)
        .map((r, m) -> r.get(0, Long.class))
        .one();
  }

  public Mono<Player> updatePlayer(Player player) {
    return dbClient.update()
        .table(Player.class)
        .using(player)
        .matching(where("id").is(player.getId()))
        .fetch()
        .rowsUpdated()
        .map(__ -> player);
  }

  public Mono<Long> findLastId() {
    return dbClient.select()
        .from(Player.class)
        .orderBy(Sort.by(Order.desc("id")))
        .map((r, m) -> r.get("id", Long.class))
        .first()
        .switchIfEmpty(Mono.just(0L));
  }

  public Mono<Integer> removeAll() {
    return dbClient.delete()
        .from(Player.class)
        .fetch()
        .rowsUpdated();
  }
}
