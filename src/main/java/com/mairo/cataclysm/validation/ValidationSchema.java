package com.mairo.cataclysm.validation;

import com.mairo.cataclysm.exception.ValidationException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationSchema {

  private final List<Supplier<Stream<String>>> prodsList;
  private Stream<String> msgs = Stream.empty();

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

  public ValidationSchema validate() {
    msgs = prodsList.stream().flatMap(Supplier::get);
    return this;
  }

  public Mono<Boolean> asMono() {
    List<String> msgList = msgs.collect(Collectors.toList());
    if (msgList.isEmpty()) {
      return Mono.just(true);
    } else {
      return Mono.error(new ValidationException(String.join(". ", msgList)));
    }
  }

  public Stream<String> getMsgs() {
    return msgs;
  }
}
