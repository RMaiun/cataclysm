package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
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
  public Mono<FoundAllPlayers> personById() {
    return playerService.findAllPlayers();
  }
}
