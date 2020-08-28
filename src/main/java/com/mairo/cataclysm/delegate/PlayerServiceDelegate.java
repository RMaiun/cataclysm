package com.mairo.cataclysm.delegate;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.exception.PlayersNotFoundException;
import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PlayerServiceDelegate {

  public List<String> lowercaseSurnames(List<String> surnameList) {
    return surnameList.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }

  public Either<Throwable, List<Player>> prepareCheckedPlayers(List<Player> players, List<String> surnames) {
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
