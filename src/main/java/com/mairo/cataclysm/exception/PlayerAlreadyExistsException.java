package com.mairo.cataclysm.exception;

public class PlayerAlreadyExistsException extends CataRuntimeException {

  public PlayerAlreadyExistsException(Long userId) {
    super(String.format("Player with given name already exists with id %d", userId));
  }
}
