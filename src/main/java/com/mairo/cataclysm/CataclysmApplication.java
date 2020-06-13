package com.mairo.cataclysm;

import com.mairo.cataclysm.dto.api.FindAllSeasonRoundsRequest;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import com.mairo.cataclysm.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.stream.Collectors;

@SpringBootApplication
public class CataclysmApplication implements CommandLineRunner {

  @Autowired
  private RoundService roundService;
  @Autowired
  private SeasonRepository sr;
  @Autowired
  private PlayerRepository pr;
  @Autowired
  private RoundRepository rr;

  public static void main(String[] args) {
    SpringApplication.run(CataclysmApplication.class, args);
  }


  @Override
  public void run(String... args) {
    FindAllSeasonRoundsRequest dto = new FindAllSeasonRoundsRequest();
    dto.setSeason("S1/2020");
    roundService.findAllRoundsInSeason(dto).log().subscribe();
//    sr.getSeason("S1/2020").log().subscribe();
//    pr.listAll().log().subscribe();
//    rr.listRoundsBySeason(1L).log().subscribe();
  }
}
