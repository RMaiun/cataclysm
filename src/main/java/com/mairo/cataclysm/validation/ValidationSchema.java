package com.mairo.cataclysm.validation;

import com.mairo.cataclysm.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

public class ValidationSchema {

  private List<Supplier<Stream<String>>> prodsList;

  public ValidationSchema() {
    this.prodsList = new ArrayList<>();
  }

  static ValidationSchema schema() {
    return new ValidationSchema();
  }

  public ValidationSchema witRule(Supplier<Stream<String>> prod) {
    this.prodsList.add(prod);
    return this;
  }

  public Mono<Boolean> validate() {
    List<String> allMsg = prodsList.stream()
        .flatMap(Supplier::get)
        .collect(Collectors.toList());
    if (allMsg.isEmpty()) {
      return Mono.just(true);
    } else {
      return Mono.error(new ValidationException(String.join(". ", allMsg)));
    }
  }
}
