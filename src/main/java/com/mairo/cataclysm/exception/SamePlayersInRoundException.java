package com.mairo.cataclysm.exception;

public class SamePlayersInRoundException extends CataRuntimeException {

  public SamePlayersInRoundException() {
    super("All players in round must be different");
  }
}
