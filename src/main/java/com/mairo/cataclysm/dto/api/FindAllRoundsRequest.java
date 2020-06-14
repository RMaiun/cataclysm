package com.mairo.cataclysm.dto.api;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindAllRoundsRequest {
  @NonNull
  private String season;
}
