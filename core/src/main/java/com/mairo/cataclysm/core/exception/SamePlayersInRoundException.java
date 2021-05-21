package com.mairo.cataclysm.core.exception;

public class SamePlayersInRoundException extends CataRuntimeException {

  public SamePlayersInRoundException() {
    super("All players in round must be different");
  }
}
