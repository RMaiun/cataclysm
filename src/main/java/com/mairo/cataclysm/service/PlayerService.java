package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.MonoSupport.eitherToMono;
import static com.mairo.cataclysm.validation.ValidationTypes.addPlayerValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.exception.PlayerAlreadyExistsException;
import com.mairo.cataclysm.exception.PlayerNotFoundException;
import com.mairo.cataclysm.helper.PlayerServiceHelper;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;
  private final UserRightsService userRightsService;
  private final PlayerServiceHelper psDelegate;

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
    return validate(dto, addPlayerValidationType)
        .flatMap(this::processPlayerAdd);
  }

  public Mono<Player> findPlayerByName(String surname) {
    return playerRepository.getPlayerByCriteria(Criteria.where("name").is(surname))
        .flatMap(maybePlayer -> MonoSupport.fromOptional(maybePlayer, new PlayerNotFoundException(surname)));
  }

  public Mono<Player> findPlayerByTid(String tid) {
    return findPlayerByCriteria(Criteria.where("tid").is(tid));
  }

  public Mono<Player> enableNotifications(String surname, String tid) {
    return findPlayerByName(surname).map(p -> p.withNotificationsEnabled(true).withTid(tid))
        .flatMap(playerRepository::updatePlayer);
  }

  public Mono<Player> updatePlayer(Player p) {
    return playerRepository.updatePlayer(p);
  }

  private Mono<Player> findPlayerByCriteria(Criteria criteria) {
    return playerRepository.getPlayerByCriteria(criteria)
        .flatMap(maybePlayer -> MonoSupport.fromOptional(maybePlayer, new PlayerNotFoundException(criteria.toString())));
  }

  private Mono<IdDto> processPlayerAdd(AddPlayerDto dto) {
    return checkUserIsAdmin(dto.getModerator())
        .flatMap(__ -> prepareIdForCheckedPlayer(dto))
        .flatMap(this::savePlayer)
        .map(IdDto::new);
  }

  private Mono<Long> savePlayer(Tuple2<AddPlayerDto, Long> t) {
    Player player = new Player(t.getT2() + 1, t.getT1().getSurname().toLowerCase(), t.getT1().getTid(), t.getT1().isAdmin(), false);
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
        .flatMap(p -> checkPlayerIsNew(p, dto));
  }

  private Mono<AddPlayerDto> checkPlayerIsNew(Optional<Player> maybePlayer, AddPlayerDto dto) {
    if (maybePlayer.isEmpty()) {
      return Mono.just(dto);
    } else {
      return Mono.error(new PlayerAlreadyExistsException(maybePlayer.get().getId()));
    }
  }

  private Mono<List<Player>> findAndCheckPlayers(List<String> surnames) {
    return playerRepository.findPlayers(surnames)
        .flatMap(players -> eitherToMono(psDelegate.prepareCheckedPlayers(players, surnames)));
  }
}
