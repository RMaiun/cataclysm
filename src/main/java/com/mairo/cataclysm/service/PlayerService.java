package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.api.AllPlayersResponse;
import com.mairo.cataclysm.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerService {

  private final PlayerRepository playerRepository;

  public PlayerService(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public Mono<Map<Long, String>> findAllPlayersAsMap() {
    return playerRepository.listAll()
        .map(list -> list.stream()
            .collect(Collectors.toMap(Player::getId, Player::getSurname)));
  }

  public Mono<AllPlayersResponse> findAllPlayers() {
    return playerRepository.listAll().map(AllPlayersResponse::new);
  }
}
