package com.mairo.cataclysm.controller;


import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.service.RoundsService;
import org.springframework.web.bind.annotation.*;
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

  @PostMapping("/add")
  public Mono<IdDto> addRound(@RequestBody AddRoundDto dto) {
    return roundsService.saveRound(dto);
  }
}
