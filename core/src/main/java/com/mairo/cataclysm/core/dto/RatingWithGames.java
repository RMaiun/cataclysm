package com.mairo.cataclysm.core.dto;

import lombok.Value;

@Value
public class RatingWithGames {
  String player;
  int rating;
  int games;
}
