package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.exception.PlayerAlreadyExistsException;
import com.mairo.cataclysm.exception.PlayersNotFoundException;
import com.mairo.cataclysm.repository.PlayerRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final UserRightsService userRightsService;

  Mono<Map<Long, String>> findAllPlayersAsMap() {
    return playerRepository.listAll()
        .map(list -> list.stream()
            .collect(Collectors.toMap(Player::getId, Player::getSurname)));
  }

  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerRepository.listAll().map(FoundAllPlayers::new);
  }

  Mono<List<Player>> checkPlayersExist(List<String> surnameList) {
    List<String> surnames = surnameList.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toList());
    return playerRepository.findPlayers(surnames)
        .flatMap(list -> {
          if (list.size() == surnames.size()) {
            return Mono.just(list);
          } else {
            List<String> foundIds = list.stream()
                .map(Player::getSurname)
                .collect(Collectors.toList());
            List<String> missedPlayers = surnames.stream()
                .filter(x -> !foundIds.contains(x))
                .collect(Collectors.toList());
            return Mono.error(new PlayersNotFoundException(missedPlayers));
          }
        });
  }

  public Mono<IdDto> addPlayer(AddPlayerDto dto) {

    Mono<Player> authorizedModerator = userRightsService.checkUserIsAdmin(dto.getModerator());

    Mono<AddPlayerDto> checkedPlayer = playerRepository.getPlayer(dto.getSurname())
        .flatMap(p -> p.isPresent() ? Mono.error(new PlayerAlreadyExistsException(p.get().getId()))
            : Mono.just(dto));

    return authorizedModerator.flatMap(__ -> Mono.zip(checkedPlayer, playerRepository.findLastId()))
        .flatMap(t -> playerRepository.savePlayer(new Player(t.getT2() + 1, t.getT1().getSurname().toLowerCase(), t.getT1().getTid(), t.getT1().isAdmin())))
        .map(IdDto::new);
  }
}
