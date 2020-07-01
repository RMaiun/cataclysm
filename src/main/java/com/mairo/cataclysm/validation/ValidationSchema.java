package com.mairo.cataclysm.validation;

import com.mairo.cataclysm.exception.ValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

@SuppressWarnings("unchecked")
public class ValidationSchema {

  private List<Supplier<List<String>>> prodsList;

  public ValidationSchema(Supplier<List<String>>... prods) {
    prodsList = Arrays.asList(prods);
  }

  static ValidationSchema schema(Supplier<List<String>>... prods) {
    return new ValidationSchema(prods);
  }

  public Mono<Boolean> validate() {
    List<String> allMsg = prodsList.stream()
        .flatMap(x -> x.get().stream())
        .collect(Collectors.toList());
    if (allMsg.isEmpty()) {
      return Mono.just(true);
    } else {
      return Mono.error(new ValidationException(String.join(". ", allMsg)));
    }
  }
}
