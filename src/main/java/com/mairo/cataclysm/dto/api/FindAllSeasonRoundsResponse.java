package com.mairo.cataclysm.dto.api;

import com.mairo.cataclysm.dto.FullRound;
import lombok.*;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FindAllSeasonRoundsResponse {

  @NonNull
  private List<FullRound> rounds;
}
