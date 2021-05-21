package com.mairo.cataclysm.core.repository;

import com.mairo.cataclysm.core.domain.Season;
import com.mairo.cataclysm.core.exception.SeasonNotFoundException;
import com.mongodb.client.result.DeleteResult;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SeasonRepository {

  public static final String ATTR_SEASON_END_NOTIFICATION = "seasonEndNotification";
  private final ReactiveMongoTemplate template;

  public Mono<Season> getSeason(String name) {
    return template.findOne(new Query(Criteria.where("name").is(name)), Season.class)
        .switchIfEmpty(Mono.error(new SeasonNotFoundException(name)));
  }

  public Mono<Season> saveSeason(Season season) {
    return template.insert(season);
  }

  public Mono<Season> updateSeason(Season season) {
    return template.save(season);
  }

  public Mono<List<Season>> listAll() {
    return template.findAll(Season.class).collectList();
  }

  public Mono<Long> removeAll() {
    return template.remove(Season.class)
        .all()
        .map(DeleteResult::getDeletedCount);
  }

  public Mono<Optional<Season>> findFirstSeasonWithoutNotification() {
    Criteria criteria = Criteria.where(ATTR_SEASON_END_NOTIFICATION).is(null);
    Query query = new Query(criteria).with(Sort.by(Direction.ASC, ATTR_SEASON_END_NOTIFICATION));
    return template.findOne(query, Season.class)
        .map(Optional::of)
        .switchIfEmpty(Mono.just(Optional.empty()));
  }
}
