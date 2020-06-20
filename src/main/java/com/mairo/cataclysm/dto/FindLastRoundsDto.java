package com.mairo.cataclysm.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindLastRoundsDto {
  @NonNull
  private String season;
  @NonNull
  private int qty;
}
