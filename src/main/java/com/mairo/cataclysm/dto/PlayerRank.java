package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import lombok.Value;

@Value
public class PlayerRank {
  String playerSurname;
  Long playerId;
  String tid;
  int rank;
  String score;
  int gamesPlayed;
  int allGames;
  int allPlayers;
}
