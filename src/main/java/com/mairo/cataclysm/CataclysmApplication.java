package com.mairo.cataclysm;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.repository.PlayerRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

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
    Flux<String> all = client.execute("select p.surname from player p where p.id > 10").as(String.class)
        .fetch().all();
    all.map(x -> {
      System.out.println(x);
      return x;
    }).subscribe();

  }
}
