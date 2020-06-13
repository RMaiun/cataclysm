package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Season;
import io.vavr.control.Option;
import lombok.Value;

import java.util.List;

@Value
public class PlayerSeasonData {
  Option<Season> season;
  List<Player> players;
}
