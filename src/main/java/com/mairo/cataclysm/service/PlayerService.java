package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.exception.PlayersNotFoundException;
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

  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerRepository.listAll().map(FoundAllPlayers::new);
  }

  public Mono<List<Player>> checkPlayersExist(List<Long> playerIdList) {
    return playerRepository.findPlayers(playerIdList)
        .flatMap(list -> {
          if (list.size() == playerIdList.size()) {
            return Mono.just(list);
          } else {
            List<Long> foundIds = list.stream()
                .map(Player::getId)
                .collect(Collectors.toList());
            List<Long> missedPlayers = playerIdList.stream()
                .filter(x -> !foundIds.contains(x))
                .collect(Collectors.toList());
            return Mono.error(new PlayersNotFoundException(missedPlayers));
          }
        });
  }
}
