package com.mairo.cataclysm;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.repository.PlayerRepository;
import io.r2dbc.spi.ConnectionFactory;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class CataclysmApplication implements CommandLineRunner {

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private DatabaseClient client;

  public static void main(String[] args) {
    SpringApplication.run(CataclysmApplication.class, args);
  }


  @Override
  public void run(String... args) {
    // Flux<Player> all = playerRepository.findAll();
    // all.map(x -> {
    //   System.out.println(x);
    //   return x;
    // }).subscribe();
    Mono<Optional<Player>> all = client
        .execute("select * from player p where p.id > 1 limit 1")
        .as(Player.class)
        .fetch().one()
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());


    all.map(x -> {
      System.out.println(x);
      return x;
    }).subscribe();

  }
}
