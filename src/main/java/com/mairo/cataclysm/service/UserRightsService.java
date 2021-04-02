package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.exception.InvalidUserRightsException;
import com.mairo.cataclysm.properties.AppProps;
import com.mairo.cataclysm.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserRightsService {

  private final PlayerRepository playerRepository;
  private final AppProps appProps;

  public Mono<Void> checkUserIsAdmin(String tid) {
    if (tid.equals(appProps.getPrivileged())) {
      return Mono.empty();
    } else {
      return playerRepository.listAll()
          .flatMap(players -> checkAdminPermissions(players, tid))
          .then();
    }
  }

  private Mono<Player> checkAdminPermissions(List<Player> players, String tid) {
    return players.stream()
        .filter(p -> tid.equals(p.getTid()) && p.isAdmin())
        .findAny()
        .map(Mono::just)
        .orElseGet(() -> Mono.error(new InvalidUserRightsException()));
  }
}
