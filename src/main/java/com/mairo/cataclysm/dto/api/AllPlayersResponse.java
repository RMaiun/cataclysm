package com.mairo.cataclysm.dto.api;

import com.mairo.cataclysm.domain.Player;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class AllPlayersResponse {
  @NonNull
  private List<Player> players;
}
