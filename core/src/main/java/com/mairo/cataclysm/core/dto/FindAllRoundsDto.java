package com.mairo.cataclysm.core.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindAllRoundsDto {
  @NonNull
  private String season;
}
