package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Round;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RoundRepository {

  private final DatabaseClient dbClient;

  public RoundRepository(DatabaseClient dbClient) {
    this.dbClient = dbClient;
  }

  public Mono<List<Round>> listRoundsBySeason(long season) {
    return dbClient.execute("select *  from round  r where r.season_id = :sid")
        .bind("sid", season)
        .as(Round.class)
        .fetch()
        .all()
        .collectList();
  }

  public Mono<List<Round>> listLastRoundsBySeason(long season, int roundsNum) {
    return dbClient.execute("select *  from round  r where r.season_id = :season order by created desc limit :num")
        .bind("season", season)
        .bind("num", roundsNum)
        .as(Round.class)
        .fetch()
        .all()
        .collectList();
  }
}
