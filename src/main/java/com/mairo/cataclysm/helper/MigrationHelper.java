package com.mairo.cataclysm.helper;

import com.mairo.cataclysm.config.DbConfiguration;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.properties.DbProps;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import com.mairo.cataclysm.utils.SeasonUtils;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoCollection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MigrationHelper {

  public static final Logger logger = LogManager.getLogger(DbConfiguration.class);

  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final DbProps dbProps;
  private final DatabaseClient databaseClient;
  private final SeasonRepository seasonRepository;
  private final PlayerRepository playerRepository;
  private final RoundRepository roundRepository;

  public void migrate() {
    createIndexes();
    mysqlDataToMongo();
  }

  public void mysqlDataToMongo() {
    Flux<String> mysqlS = databaseClient.execute("select name from season")
        .as(String.class)
        .fetch()
        .all();
    Flux<P> mysqlP = databaseClient.execute("select surname, tid, admin, enable_notifications from player")
        .as(P.class)
        .fetch().all();

    Flux<R> mysqlR = databaseClient.execute("select p1.surname, p2.surname, p3.surname, p4.surname, round.shutout, round.created, s.name from round"
        + "    inner join player p1 on p1.id = round.winner1_id"
        + "    inner join player p2 on p2.id = round.winner2_id"
        + "    inner join player p3 on p3.id = round.loser1_id"
        + "    inner join player p4 on p4.id = round.loser2_id"
        + "    inner join season s on round.season_id = s.id"
        + "    order by created ASC ;")
        .as(R.class)
        .fetch()
        .all();

    seasonRepository.removeAll()
        .then(playerRepository.removeAll())
        .then(roundRepository.removeAll())
        .thenMany(mysqlS)
        .flatMap(s -> seasonRepository.saveSeason(Season.of(s, ZonedDateTime.of(SeasonUtils.seasonGate(s).getRight(), LocalTime.now(), ZoneOffset.UTC))))
        .thenMany(mysqlP.flatMap(p -> playerRepository.savePlayer(new Player(null, p.surname, p.tid, p.admin, p.notificationsEnabled))))
        .thenMany(mysqlR.flatMap(r -> {
          System.out.println(r.getCreated());
          ZonedDateTime zdt = ZonedDateTime.of(r.getCreated().toLocalDate(), r.getCreated().toLocalTime(), ZoneOffset.UTC);
          System.out.println(zdt);
          return roundRepository.saveRound(new Round(null, r.winner1, r.winner2, r.loser1, r.loser2, r.shutout, r.season, zdt));
        }))
        .subscribe();
  }

  public void createIndexes() {
    BiFunction<String, ReactiveMongoTemplate, Function<Throwable, Mono<MongoCollection<Document>>>> errorFallback =
        (collection, rmt) -> throwable -> Mono.just(throwable)
            .doOnNext(err -> logger.info(String.format("Collection %s is already created", collection)))
            .then(rmt.getCollection(collection));
    if (dbProps.isRecreateMongoIndexes()) {
      Mono<MongoCollection<Document>> seasonCollection = reactiveMongoTemplate.createCollection(Season.class);
      Mono<MongoCollection<Document>> playerCollection = reactiveMongoTemplate.createCollection(Player.class);
      Mono<MongoCollection<Document>> roundCollection = reactiveMongoTemplate.createCollection(Round.class);
      seasonCollection
          .onErrorResume(errorFallback.apply("season", reactiveMongoTemplate))
          .doOnNext(MongoCollection::dropIndexes)
          .doOnNext(collection -> logger.info("Indexes were dropped for collection 'season'"))
          .doOnNext(c -> c.createIndexes(List.of(
              new IndexModel(Indexes.ascending("_id"), new IndexOptions().unique(true)),
              new IndexModel(Indexes.ascending("name"), new IndexOptions().unique(true)))))
          .doOnNext(collection -> logger.info("Indexes were created for collection 'season'"))
          .then(playerCollection
              .onErrorResume(errorFallback.apply("player", reactiveMongoTemplate))
              .doOnNext(MongoCollection::dropIndexes)
              .doOnNext(collection -> logger.info("Indexes were dropped for collection 'player'"))
              .doOnNext(c -> c.createIndexes(List.of(
                  new IndexModel(Indexes.ascending("_id"), new IndexOptions().unique(true)),
                  new IndexModel(Indexes.ascending("surname"), new IndexOptions().unique(true))))))
          .doOnNext(collection -> logger.info("Indexes were created for collection 'player'"))
          .then(roundCollection
              .onErrorResume(errorFallback.apply("round", reactiveMongoTemplate))
              .doOnNext(MongoCollection::dropIndexes)
              .doOnNext(collection -> logger.info("Indexes were dropped for collection 'round'"))
              .doOnNext(c -> c.createIndexes(List.of(
                  new IndexModel(Indexes.ascending("_id"), new IndexOptions().unique(true)),
                  new IndexModel(Indexes.ascending("season"))))))
          .doOnNext(collection -> logger.info("Indexes were created for collection 'round'"))
          .subscribe();
    }
  }

  @Data
  @NoArgsConstructor
  static class R {

    private String winner1;
    private String winner2;
    private String loser1;
    private String loser2;
    private boolean shutout;
    private LocalDateTime created;
    private String season;
  }

  @Data
  @NoArgsConstructor
  static class P {

    private String surname;
    private String tid;
    private boolean admin;
    private boolean notificationsEnabled;
  }
}
