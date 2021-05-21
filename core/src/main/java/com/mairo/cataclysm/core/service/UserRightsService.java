package com.mairo.cataclysm.core.service;


import com.mairo.cataclysm.core.domain.Player;

import com.mairo.cataclysm.core.exception.AuthorizationRuntimeException;
import com.mairo.cataclysm.core.exception.InvalidUserRightsException;
import com.mairo.cataclysm.core.properties.AppProps;
import com.mairo.cataclysm.core.repository.PlayerRepository;
import com.mairo.cataclysm.core.utils.MonoSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserRightsService {

  private final PlayerRepository playerRepository;
  private final AppProps appProps;

  public Mono<Player> checkUserIsAdmin(String tid) {
    if (tid.equals(appProps.getPrivileged())) {
      return Mono.empty();
    } else {
      return playerRepository.listAll()
          .flatMap(players -> checkAdminPermissions(players, tid));
    }
  }

  public Mono<Player> checkUserIsRegistered(String tid){
    return playerRepository.getPlayerByCriteria(Criteria.where("tid").is(tid))
        .flatMap(maybeUser -> MonoSupport.fromOptional(maybeUser, new AuthorizationRuntimeException()));
  }

  private Mono<Player> checkAdminPermissions(List<Player> players, String tid) {
    return players.stream()
        .filter(p -> tid.equals(p.getTid()) && p.isAdmin())
        .findAny()
        .map(Mono::just)
        .orElseGet(() -> Mono.error(new InvalidUserRightsException()));
  }
}
