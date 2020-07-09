package com.mairo.cataclysm.validation;

import reactor.core.publisher.Mono;

public class Validator {

  private Validator() {
  }

  public static <T> Mono<T> validate(T dto, ValidationType<T> validationType) {
    return validationType.describeSchema(dto).validate().asMono().map(__ -> dto);
  }
}
