package com.mairo.cataclysm.core.dto;

import java.util.List;
import lombok.Data;

@Data
public class SeasonShortStats {
  private String season;
  private List<PlayerStats> playersRating;
  private int gamesPlayed;
  private int daysToSeasonEnd;
  private Streak bestStreak;
  private Streak worstStreak;
}
