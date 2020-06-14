package com.mairo.cataclysm.dto.api;

import com.mairo.cataclysm.dto.FullRound;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FindLastRoundsResponse {
  @NonNull
  private List<FullRound> rounds;
}
