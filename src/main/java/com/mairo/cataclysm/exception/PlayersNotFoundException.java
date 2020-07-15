package com.mairo.cataclysm.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PlayersNotFoundException extends CataRuntimeException {
  public PlayersNotFoundException(List<Long> missedPlayers) {
    super(String.format("Players with id: [%s] were not found", StringUtils.join(",", missedPlayers)));
  }
}
