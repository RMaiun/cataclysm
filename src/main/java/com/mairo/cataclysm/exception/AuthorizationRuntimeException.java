package com.mairo.cataclysm.exception;

public class AuthorizationRuntimeException extends CataRuntimeException {

  public AuthorizationRuntimeException() {
    super("User is not authorized");
  }
}
