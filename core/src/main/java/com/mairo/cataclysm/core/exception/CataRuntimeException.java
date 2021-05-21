package com.mairo.cataclysm.core.exception;

public class CataRuntimeException extends RuntimeException{
  public CataRuntimeException() {
  }

  public CataRuntimeException(String message) {
    super(message);
  }

  public CataRuntimeException(Throwable cause) {
    super(cause);
  }
}
