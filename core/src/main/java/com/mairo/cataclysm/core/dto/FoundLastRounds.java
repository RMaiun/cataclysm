package com.mairo.cataclysm.core.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoundLastRounds {

  private String season;
  private List<FullRound> rounds;
}
