package com.mairo.cataclysm.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.exception.SeasonNotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SeasonRepository {

  private final DatabaseClient dbClient;

  public SeasonRepository(DatabaseClient dbClient) {
    this.dbClient = dbClient;
  }

  public Mono<Season> getSeason(String name) {
    return dbClient.select()
        .from(Season.class)
        .matching(where("name").is(name))
        .as(Season.class)
        .one()
        .switchIfEmpty(Mono.error(new SeasonNotFoundException(name)));
  }

  public Mono<Integer> saveSeason(Season season) {
    return dbClient.insert()
        .into(Season.class)
        .using(season)
        .fetch()
        .rowsUpdated();
  }

  public Mono<List<Season>> listAll() {
    return dbClient.select()
        .from(Season.class)
        .as(Season.class)
        .all().collectList();
  }

  public Mono<Long> findLastId() {
    return dbClient.select()
        .from(Season.class)
        .orderBy(Sort.by(Order.desc("id")))
        .map((r, m) -> r.get("id", Long.class))
        .first()
        .switchIfEmpty(Mono.just(0L));
  }

  public Mono<Integer> removeAll() {
    return dbClient.delete()
        .from(Season.class)
        .fetch()
        .rowsUpdated();
  }
}
