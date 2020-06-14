package com.mairo.cataclysm.http;


import com.mairo.cataclysm.dto.api.FindAllRoundsRequest;
import com.mairo.cataclysm.dto.api.FindAllRoundsResponse;
import com.mairo.cataclysm.dto.api.FindLastRoundsRequest;
import com.mairo.cataclysm.dto.api.FindLastRoundsResponse;
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

  @GetMapping("/findAll/{season}")
  public Mono<FindAllRoundsResponse> findAllRounds(@PathVariable String season) {
    return roundsService.findAllRoundsInSeason(new FindAllRoundsRequest(season));
  }

  @GetMapping("/findLast/{season}/{qty}")
  public Mono<FindLastRoundsResponse> findAllRounds(@PathVariable String season, @PathVariable int qty) {
    return roundsService.findLastRoundsInSeason(new FindLastRoundsRequest(season, qty));
  }
}
