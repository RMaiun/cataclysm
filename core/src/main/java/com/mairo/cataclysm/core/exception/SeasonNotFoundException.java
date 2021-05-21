package com.mairo.cataclysm.core.exception;

public class SeasonNotFoundException extends CataRuntimeException {
  public SeasonNotFoundException(String seasonName) {
    super(String.format("Season %s is not found", seasonName));
  }
}
