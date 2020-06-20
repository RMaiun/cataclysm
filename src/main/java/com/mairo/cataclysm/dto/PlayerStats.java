package com.mairo.cataclysm.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PlayerStats {
  @NonNull
  private String surname;
  @NonNull
  private Integer score;
}
