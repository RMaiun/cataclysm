package com.mairo.cataclysm.core.helper;

import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.exception.PlayersNotFoundException;
import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class PlayerServiceHelper {

  public static final Logger log = LogManager.getLogger(PlayerServiceHelper.class);

  public List<String> lowercaseSurnames(List<String> surnameList) {
    return surnameList.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }

  public Either<Throwable, List<Player>> prepareCheckedPlayers(List<Player> players, List<String> surnames) {
    String found = players.stream()
        .map(Player::getSurname)
        .collect(Collectors.joining(","));
    String expected = String.join(",", surnames);
    log.info("Expected players: {}, found players: {}", expected, found);
    if (players.size() == surnames.size()) {
      return Either.right(players);
    } else {
      List<String> foundIds = players.stream()
          .map(Player::getSurname)
          .collect(Collectors.toList());
      List<String> missedPlayers = surnames.stream()
          .filter(x -> !foundIds.contains(x))
          .collect(Collectors.toList());
      return Either.left(new PlayersNotFoundException(missedPlayers));
    }
  }
}
