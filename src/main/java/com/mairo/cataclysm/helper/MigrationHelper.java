package com.mairo.cataclysm.helper;

import com.mairo.cataclysm.config.DbConfiguration;
import com.mairo.cataclysm.domain.AuditLog;
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
    // mysqlDataToMongo();
  }

  public void mysqlDataToMongo() {
    Flux<String> mysqlS = databaseClient.execute("select name from season")
        .as(String.class)
        .fetch()
        .all();
    Flux<P> mysqlP = databaseClient.execute("select surname, tid, admin, enable_notifications as notificationsEnabled from player")
        .map(r -> {
          P p = new P();
          p.setSurname((String) r.get(0));
          p.setTid((String) r.get(1));

          p.setAdmin((Byte) r.get(2) == 1);
          p.setNotificationsEnabled((Byte) r.get(3) == 1);
          return p;
        })
        .all().log();

    Flux<R> mysqlR = databaseClient.execute(
        "select p1.surname as w1, "
            + "p2.surname as w2, "
            + "p3.surname as l1, "
            + "p4.surname as l2, "
            + "round.shutout as shutout, "
            + "round.created as created, "
            + "s.name as season from round"
        + "    inner join player p1 on p1.id = round.winner1_id"
        + "    inner join player p2 on p2.id = round.winner2_id"
        + "    inner join player p3 on p3.id = round.loser1_id"
        + "    inner join player p4 on p4.id = round.loser2_id"
        + "    inner join season s on round.season_id = s.id"
        + "    order by created ASC ;")
        .as(R.class)
        .fetch()
        .all().log();

    seasonRepository.removeAll()
        .then(playerRepository.removeAll())
        .then(roundRepository.removeAll())
        .thenMany(mysqlS)
        .flatMap(s -> seasonRepository.saveSeason(Season.of(s, ZonedDateTime.of(SeasonUtils.seasonGate(s).getRight(), LocalTime.now(), ZoneOffset.UTC))))
        .thenMany(mysqlP.flatMap(p -> playerRepository.savePlayer(new Player(null, p.surname, p.tid, p.admin, p.notificationsEnabled))))
        .thenMany(mysqlR.flatMap(r -> {
          ZonedDateTime zdt = ZonedDateTime.of(r.getCreated().toLocalDate(), r.getCreated().toLocalTime(), ZoneOffset.UTC);
          return roundRepository.saveRound(new Round(null, r.w1, r.w2, r.l1, r.l2, r.shutout, r.season, zdt));
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
      Mono<MongoCollection<Document>> auditLogCollection = reactiveMongoTemplate.createCollection(AuditLog.class);
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
          .then(auditLogCollection
              .onErrorResume(errorFallback.apply("auditLog", reactiveMongoTemplate))
              .doOnNext(MongoCollection::dropIndexes)
              .doOnNext(collection -> logger.info("Indexes were dropped for collection 'auditLog'"))
              .doOnNext(c -> c.createIndexes(List.of(
                  new IndexModel(Indexes.ascending("_id"), new IndexOptions().unique(true)))))
          .doOnNext(collection -> logger.info("Indexes were created for collection 'auditLog'")))
          .subscribe();
    }
  }

  @Data
  @NoArgsConstructor
  static class R {

    private String w1;
    private String w2;
    private String l1;
    private String l2;
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
