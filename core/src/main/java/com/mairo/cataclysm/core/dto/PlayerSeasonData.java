package com.mairo.cataclysm.core.dto;

import com.mairo.cataclysm.core.domain.Player;
import java.util.List;
import lombok.Value;
import lombok.With;

@Value
@With
public class PlayerSeasonData {

  String season;
  List<Player> players;
}
