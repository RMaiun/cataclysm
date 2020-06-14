package com.mairo.cataclysm.http;

import com.mairo.cataclysm.dto.api.AllPlayersResponse;
import com.mairo.cataclysm.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("player")
public class PlayerController {

  private final PlayerService playerService;


  public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @GetMapping("/all")
  public Mono<AllPlayersResponse> personById() {
    return playerService.findAllPlayers();
  }
}
