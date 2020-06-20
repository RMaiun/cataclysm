package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class FoundAllPlayers {
  @NonNull
  private List<Player> players;
}
