package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.core.dto.AddPlayerDto;
import com.mairo.cataclysm.core.dto.FoundAllPlayers;
import com.mairo.cataclysm.core.dto.IdDto;
import com.mairo.cataclysm.core.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("players")
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;


  @GetMapping("/all")
  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerService.findAllPlayers();
  }

  @PostMapping("/add")
  public Mono<IdDto> addPlayer(@RequestBody AddPlayerDto addPlayerDto) {
    return playerService.addPlayer(addPlayerDto);
  }
}
