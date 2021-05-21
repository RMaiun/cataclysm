package com.mairo.cataclysm.core.dto;

import com.mairo.cataclysm.core.domain.Player;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class FoundAllPlayers {
  @NonNull
  private List<Player> players;
}
