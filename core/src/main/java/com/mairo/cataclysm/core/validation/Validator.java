package com.mairo.cataclysm.core.validation;

import reactor.core.publisher.Mono;

public class Validator {

  private Validator() {
  }

  public static <T> Mono<T> validate(T dto, ValidationType<T> validationType) {
    return validationType.applyDto(dto).validate().asMono().map(__ -> dto);
  }
}
