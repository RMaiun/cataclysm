package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
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
