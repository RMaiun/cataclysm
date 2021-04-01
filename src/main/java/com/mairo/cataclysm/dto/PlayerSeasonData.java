package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Season;
import java.util.List;
import java.util.Map;
import lombok.Value;
import lombok.With;

@Value
@With
public class PlayerSeasonData {

  String season;
  List<Player> players;
}
