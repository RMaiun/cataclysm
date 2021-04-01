package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.exception.SeasonNotFoundException;
import com.mongodb.client.result.DeleteResult;
import java.util.List;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SeasonRepository {

  private final ReactiveMongoTemplate template;

  public SeasonRepository(ReactiveMongoTemplate template) {
    this.template = template;
  }

  public Mono<Season> getSeason(String name) {
    return template.findOne(new Query(Criteria.where("name").is(name)), Season.class)
        .switchIfEmpty(Mono.error(new SeasonNotFoundException(name)));
  }

  public Mono<Season> saveSeason(Season season) {
    return template.insert(season);
  }

  public Mono<List<Season>> listAll() {
    return template.findAll(Season.class).collectList();
  }

  public Mono<Long> removeAll() {
    return template.remove(Season.class)
        .all()
        .map(DeleteResult::getDeletedCount);
  }
}
