package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.FuncPredef.toMono;

import com.mairo.cataclysm.delegate.PlayerServiceDelegate;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.exception.PlayerAlreadyExistsException;
import com.mairo.cataclysm.repository.PlayerRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final UserRightsService userRightsService;
  private final PlayerServiceDelegate psDelegate;

  Mono<Map<Long, String>> findAllPlayersAsMap() {
    return playerRepository.listAll()
        .map(list -> list.stream()
            .collect(Collectors.toMap(Player::getId, Player::getSurname)));
  }

  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerRepository.listAll().map(FoundAllPlayers::new);
  }

  Mono<List<Player>> checkPlayersExist(List<String> surnameList) {
    return Mono.just(psDelegate.lowercaseSurnames(surnameList))
        .flatMap(this::findAndCheckPlayers);
  }

  public Mono<IdDto> addPlayer(AddPlayerDto dto) {
    return checkUserIsAdmin(dto.getModerator())
        .flatMap(__ -> prepareIdForCheckedPlayer(dto))
        .flatMap(this::savePlayer)
        .map(IdDto::new);
  }

  private Mono<Long> savePlayer(Tuple2<AddPlayerDto, Long> t) {
    Player player = new Player(t.getT2() + 1, t.getT1().getSurname().toLowerCase(), t.getT1().getTid(), t.getT1().isAdmin());
    return playerRepository.savePlayer(player);
  }

  private Mono<Tuple2<AddPlayerDto, Long>> prepareIdForCheckedPlayer(AddPlayerDto dto) {
    return Mono.zip(checkPlayerNotExist(dto), playerRepository.findLastId());
  }

  private Mono<Player> checkUserIsAdmin(String moderator) {
    return userRightsService.checkUserIsAdmin(moderator);
  }

  private Mono<AddPlayerDto> checkPlayerNotExist(AddPlayerDto dto) {
    return playerRepository.getPlayer(dto.getSurname())
        .flatMap(p -> p.isPresent()
            ? Mono.error(new PlayerAlreadyExistsException(p.get().getId()))
            : Mono.just(dto));
  }

  private Mono<List<Player>> findAndCheckPlayers(List<String> surnames) {
    return playerRepository.findPlayers(surnames)
        .flatMap(players -> toMono(psDelegate.prepareCheckedPlayers(players, surnames)));
  }
}
