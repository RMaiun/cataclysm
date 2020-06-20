package com.mairo.cataclysm.controller;


import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.service.RoundsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("round")
public class RoundController {

  private final RoundsService roundsService;

  public RoundController(RoundsService roundsService) {
    this.roundsService = roundsService;
  }

  @GetMapping("/findLast/{season}/{qty}")
  public Mono<FoundLastRounds> findAllRounds(@PathVariable String season, @PathVariable int qty) {
    return roundsService.findLastRoundsInSeason(season, qty)
        .map(FoundLastRounds::new);
  }
}
