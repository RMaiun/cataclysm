package com.mairo.cataclysm.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
  private long roundsPlayed;
}
