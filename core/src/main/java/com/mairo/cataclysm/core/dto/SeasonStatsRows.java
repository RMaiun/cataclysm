package com.mairo.cataclysm.core.dto;

import java.util.List;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SeasonStatsRows {
  @NonNull
  private List<String> headers;
  @NonNull
  private List<Integer> totals;
  @NonNull
  private List<List<String>> games;
  @NonNull
  private List<String> createdDates;
  @NonNull
  private Integer roundsPlayed;
}
