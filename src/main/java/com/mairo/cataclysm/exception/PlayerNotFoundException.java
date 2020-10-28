package com.mairo.cataclysm.exception;

public class PlayerNotFoundException extends CataRuntimeException {

  public PlayerNotFoundException(String message) {
    super(String.format("Player %s is not found", message));
  }
}
