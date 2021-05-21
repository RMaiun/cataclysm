package com.mairo.cataclysm.core.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.mairo.cataclysm.core.domain.Player;
import com.mongodb.client.result.DeleteResult;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerRepository {

  private final ReactiveMongoTemplate template;

  public Mono<List<Player>> listAll() {
    return template.findAll(Player.class)
        .collectList()
        .map(list -> list.stream()
            .sorted(Comparator.comparing(Player::getId))
            .collect(Collectors.toList()));
  }

  public Mono<List<Player>> findPlayers(List<String> surnames) {
    return template.find(new Query().addCriteria(where("surname").in(surnames)), Player.class)
        .collectList();
  }

  public Mono<Optional<Player>> getPlayer(String name) {
    return template.findOne(new Query().addCriteria(where("surname").is(name)), Player.class)
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()));
  }

  public Mono<Optional<Player>> getPlayerByCriteria(Criteria criteria) {
    return template.findOne(new Query(criteria), Player.class)
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()));
  }

  public Mono<Player> savePlayer(Player player) {
    return template.save(player);
  }

  public Mono<Player> updatePlayer(Player player) {
    return template.save(player);
  }

  public Mono<Long> removeAll() {
    return template.remove(Player.class)
        .all()
        .map(DeleteResult::getDeletedCount);
  }
}
