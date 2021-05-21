package com.mairo.cataclysm.core.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.mairo.cataclysm.core.domain.Round;
import com.mongodb.client.result.DeleteResult;
import java.time.ZonedDateTime;
import java.util.List;
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
public class RoundRepository {

  private final ReactiveMongoTemplate template;

  public Mono<List<Round>> listRoundsBySeason(String season) {
    return template.find(new Query().addCriteria(where("season").is(season)), Round.class)
        .collectList();
  }

  public Mono<List<Round>> listLastRoundsBySeason(String season, int roundsNum) {
    Criteria criteria = Criteria.where("season").is(season);
    Query query = new Query(criteria)
        .with(Sort.by(Direction.DESC, "created")).limit(roundsNum);
    return template.find(query, Round.class).collectList();
  }

  public Mono<Round> saveRound(Round round) {
    return template.insert(round);
  }

  public Mono<List<Round>> listLastRoundsBeforeDate(ZonedDateTime before) {
    Criteria criteria = Criteria.where("created").lte(before);
    return template.find(new Query(criteria), Round.class).collectList();
  }

  public Mono<Long> removeAll() {
    return template.remove(Round.class).all().map(DeleteResult::getDeletedCount);
  }

}
