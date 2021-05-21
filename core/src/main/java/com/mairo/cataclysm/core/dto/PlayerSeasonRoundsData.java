package com.mairo.cataclysm.core.dto;

import com.mairo.cataclysm.core.domain.Round;
import java.util.List;
import lombok.Value;
import lombok.With;

@Value
@With
public class PlayerSeasonRoundsData {

  String season;
  List<String> players;
  List<Round> rounds;
}
