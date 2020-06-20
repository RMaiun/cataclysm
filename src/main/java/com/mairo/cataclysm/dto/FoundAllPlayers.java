package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FoundAllPlayers {
  @NonNull
  private List<Player> players;
}
