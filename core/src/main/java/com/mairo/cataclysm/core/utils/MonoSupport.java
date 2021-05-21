package com.mairo.cataclysm.core.utils;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.util.Optional;
import reactor.core.publisher.Mono;

public class MonoSupport {

  private MonoSupport() {

  }

  public static <T> Mono<T> eitherToMono(Either<Throwable, T> either) {
    if (either.isLeft()) {
      return Mono.error(either.getLeft());
    }
    return Mono.just(either.get());
  }

  public static <T> Mono<T> fromTry(Try<T> fa) {
    return eitherToMono(fa.toEither());
  }

  public static <T> Mono<T> fromTry(CheckedFunction0<? extends T> supplier) {
    return Mono.fromCallable(() -> supplier).flatMap(s -> fromTry(Try.of(s)));
  }

  public static <T> Mono<T> fromOptional(Optional<T> opt, Throwable throwable) {
    return Mono.justOrEmpty(opt).switchIfEmpty(Mono.error(throwable));
  }
}
