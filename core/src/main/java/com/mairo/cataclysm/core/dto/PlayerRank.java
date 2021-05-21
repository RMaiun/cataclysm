package com.mairo.cataclysm.core.dto;

import lombok.Value;

@Value
public class PlayerRank {
  String playerSurname;
  String tid;
  int rank;
  String score;
  int gamesPlayed;
  int allGames;
  int allPlayers;
}
