package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Season;
import lombok.Value;

import java.util.Map;

@Value
public class PlayerSeasonData {
  Season season;
  Map<Long, String> players;
}
