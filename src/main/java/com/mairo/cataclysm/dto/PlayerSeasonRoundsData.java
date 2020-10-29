package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import lombok.Value;
import lombok.With;

import java.util.List;
import java.util.Map;

@Value
@With
public class PlayerSeasonRoundsData {
  Season season;
  Map<Long, String> players;
  List<Round> rounds;
}
