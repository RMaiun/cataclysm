package com.mairo.cataclysm.controller;


import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.model.RoundsModel;
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

  private final RoundsModel roundsModel;

  public RoundController(RoundsModel roundsModel) {
    this.roundsModel = roundsModel;
  }

  @GetMapping("/findLast/{season}/{qty}")
  public Mono<FoundLastRounds> findAllRounds(@PathVariable String season, @PathVariable int qty) {
    return roundsModel.findLastRoundsInSeason(new FindLastRoundsDto(season, qty));
  }

  @PostMapping("/add")
  public Mono<IdDto> addRound(@RequestBody AddRoundDto dto) {
    return roundsModel.saveRound(dto);
  }
}
