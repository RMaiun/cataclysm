package com.mairo.cataclysm.model;

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
import com.mairo.cataclysm.service.UserRightsService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerModel {

  public static final Logger logger = LogManager.getLogger(PlayerModel.class);

  private final PlayerRepository playerRepository;
  private final UserRightsService userRightsService;
  private final PlayerServiceHelper psDelegate;

  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerRepository.listAll()
        .doOnNext(players -> logger.info("Found {} players", players.size()))
        .map(FoundAllPlayers::new);
  }

  public Mono<List<String>> findAllPlayerNames() {
    return playerRepository.listAll()
        .map(list -> list.stream().map(Player::getSurname).collect(Collectors.toList()))
        .doOnNext(players -> logger.info("Found {} players", players.size()));
  }

  public Mono<List<Player>> checkPlayersExist(List<String> surnameList) {
    return Mono.just(psDelegate.lowercaseSurnames(surnameList))
        .flatMap(this::findAndCheckPlayers);
  }

  public Mono<IdDto> addPlayer(AddPlayerDto dto) {
    return validate(dto, addPlayerValidationType)
        .then(processPlayerAdd(dto));
  }

  public Mono<Player> findPlayerByName(String surname) {
    return playerRepository.getPlayerByCriteria(Criteria.where("surname").is(surname))
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
    // return checkUserIsAdmin(dto.getModerator())
    //     .then(checkPlayerNotExist(dto))
    //     .then(savePlayer(dto))
    //     .map(IdDto::new);
    return
        checkPlayerNotExist(dto)
        .then(savePlayer(dto))
        .map(IdDto::new);
  }

  private Mono<String> savePlayer(AddPlayerDto dto) {
    Player player = new Player(null, dto.getSurname().toLowerCase(), dto.getTid(), dto.isAdmin(), false);
    return playerRepository.savePlayer(player).map(Player::getId);
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
