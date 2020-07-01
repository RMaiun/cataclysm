package com.mairo.cataclysm.validation;

import java.util.function.Function;
import reactor.core.publisher.Mono;

public class Validator {

  private Validator() {
  }

  public static <T> Mono<T> validate(T dto, Function<T, ValidationSchema> schemaF) {
    return schemaF.apply(dto).validate().map(__ -> dto);
  }
}
