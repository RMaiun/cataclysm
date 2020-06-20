package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.dto.FullRound;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FoundLastRounds {
  @NonNull
  private List<FullRound> rounds;
}
