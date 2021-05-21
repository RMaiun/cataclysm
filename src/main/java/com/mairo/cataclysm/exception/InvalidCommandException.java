package com.mairo.cataclysm.exception;

import com.mairo.cataclysm.core.exception.CataRuntimeException;

public class InvalidCommandException extends CataRuntimeException {

  public InvalidCommandException(String cmd) {
    super(String.format("Command %s is not supported.",cmd));
  }
}
