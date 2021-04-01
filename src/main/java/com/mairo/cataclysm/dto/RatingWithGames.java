package com.mairo.cataclysm.dto;

import lombok.Value;

@Value
public class RatingWithGames {
  String player;
  int rating;
  int games;
}
