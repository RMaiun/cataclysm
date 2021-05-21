package com.mairo.cataclysm.core.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PlayerStats {

  @NonNull
  private String surname;
  @NonNull
  private String score;
  @NonNull
  private Integer games;
}
