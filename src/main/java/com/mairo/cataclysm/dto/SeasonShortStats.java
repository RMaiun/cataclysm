package com.mairo.cataclysm.dto;

import lombok.Data;

import java.util.List;

@Data
public class SeasonShortStats {
  private String season;
  private List<PlayerStats> playersRating;
  private int gamesPlayed;
  private int daysToSeasonEnd;
  private Streak bestStreak;
  private Streak worstStreak;
}
