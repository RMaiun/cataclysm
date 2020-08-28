package com.mairo.cataclysm.dto;

import lombok.Value;

@Value
public class RatingWithGames {

  long pid;
  String player;
  int rating;
  int games;
}
