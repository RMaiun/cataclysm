package com.mairo.cataclysm.dto.api;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindLastRoundsRequest {
  @NonNull
  private String season;
  @NonNull
  private int qty;
}
