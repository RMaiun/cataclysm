package com.mairo.cataclysm.controller;

import static com.mairo.cataclysm.validation.ValidationTypes.addPlayerValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("players")
public class PlayerController {

  private final PlayerService playerService;


  public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @GetMapping("/all")
  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerService.findAllPlayers();
  }

  @PostMapping("/add")
  public Mono<IdDto> addPlayer(@RequestBody AddPlayerDto addPlayerDto) {
    return validate(addPlayerDto, addPlayerValidationType)
        .flatMap(playerService::addPlayer);
  }
}
