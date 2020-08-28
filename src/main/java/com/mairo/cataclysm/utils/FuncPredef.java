package com.mairo.cataclysm.utils;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public class FuncPredef {

  private FuncPredef() {

  }

  public static <T> Mono<T> toMono(Either<Throwable, T> either) {
    if (either.isLeft()) {
      return Mono.error(either.getLeft());
    }
    return Mono.just(either.get());
  }
}
