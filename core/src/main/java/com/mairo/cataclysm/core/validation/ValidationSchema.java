package com.mairo.cataclysm.core.validation;

import com.mairo.cataclysm.core.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

public class ValidationSchema {

  private final List<Supplier<Stream<String>>> prodsList;
  private Stream<String> msgs = Stream.empty();

  private ValidationSchema() {
    this.prodsList = new ArrayList<>();
  }

  static ValidationSchema schema() {
    return new ValidationSchema();
  }

  public ValidationSchema withRule(Supplier<Stream<String>> prod) {
    this.prodsList.add(prod);
    return this;
  }

  ValidationSchema validate() {
    msgs = prodsList.stream().flatMap(Supplier::get);
    return this;
  }

  Mono<Boolean> asMono() {
    List<String> msgList = msgs.collect(Collectors.toList());
    if (msgList.isEmpty()) {
      return Mono.just(true);
    } else {
      return Mono.error(new ValidationException(String.join(". ", msgList)));
    }
  }

  Stream<String> getMsgs() {
    return msgs;
  }
}
