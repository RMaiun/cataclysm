package com.mairo.cataclysm.controller;


import static com.mairo.cataclysm.validation.ValidationTypes.addRoundValidationType;
import static com.mairo.cataclysm.validation.ValidationTypes.listLastRoundsValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.service.RoundsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("rounds")
public class RoundController {

  private final RoundsService roundsService;

  public RoundController(RoundsService roundsService) {
    this.roundsService = roundsService;
  }

  @GetMapping("/findLast/{season}/{qty}")
  public Mono<FoundLastRounds> findAllRounds(@PathVariable String season, @PathVariable int qty) {
    return validate(new FindLastRoundsDto(season, qty), listLastRoundsValidationType)
        .flatMap(roundsService::findLastRoundsInSeason)
        .map(FoundLastRounds::new);
  }

  @PostMapping("/add")
  public Mono<IdDto> addRound(@RequestBody AddRoundDto dto) {
    return validate(dto, addRoundValidationType)
        .flatMap(x -> roundsService.saveRound(dto));
  }
}
