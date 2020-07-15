package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.exception.SeasonNotFoundException;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

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
}
