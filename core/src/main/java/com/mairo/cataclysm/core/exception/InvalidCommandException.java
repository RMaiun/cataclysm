package com.mairo.cataclysm.core.exception;

public class InvalidCommandException extends CataRuntimeException {

  public InvalidCommandException(String cmd) {
    super(String.format("Command %s is not supported.",cmd));
  }
}
