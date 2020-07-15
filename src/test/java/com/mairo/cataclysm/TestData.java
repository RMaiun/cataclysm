package com.mairo.cataclysm;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface TestData {

  static List<Player> testPlayers() {
    Player p1 = new Player(1L, "player1");
    Player p2 = new Player(2L, "player2");
    Player p3 = new Player(3L, "player3");
    Player p4 = new Player(4L, "player4");
    return Arrays.asList(p1, p2, p3, p4);
  }

  static List<Round> testRounds() {
    Round r1 = new Round(1L, 1L, 2L, 3L, 4L,
        false, 1L, LocalDateTime.now());
    Round r2 = new Round(1L, 1L, 3L, 2L, 4L,
        false, 1L, LocalDateTime.now());
    Round r3 = new Round(1L, 1L, 4L, 2L, 4L,
        false, 1L, LocalDateTime.now());
    Round r4 = new Round(1L, 1L, 2L, 3L, 4L,
        false, 1L, LocalDateTime.now());
    return Arrays.asList(r1, r2, r3, r4);
  }

  static List<Player> players(List<Long> ids){
    return ids.stream().map(id -> new Player(id, id.toString())).collect(Collectors.toList());
  }


}
