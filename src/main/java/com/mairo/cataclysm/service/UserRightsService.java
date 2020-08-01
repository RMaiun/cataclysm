package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.exception.InvalidUserRightsException;
import com.mairo.cataclysm.repository.PlayerRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserRightsService {

  private final PlayerRepository playerRepository;

  public Mono<Player> checkUserIsAdmin(String tid) {
    return playerRepository.listAll()
        .flatMap(list -> {
          Optional<Player> player = list.stream()
              .filter(p -> tid.equals(p.getTid()) && p.isAdmin())
              .findAny();
          if (player.isPresent()) {
            return Mono.just(player.get());
          } else {
            return Mono.error(new InvalidUserRightsException());
          }
        });
  }
}
