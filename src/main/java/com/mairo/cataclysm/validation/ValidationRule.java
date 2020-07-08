package com.mairo.cataclysm.validation;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ValidationRule {

  public static <T> Stream<String> executeRule(T data, String field, List<ValidationFunction<T>> validators) {
    ValueField<T> tValueField = new ValueField<>(data, field);
    if (isNull(data)) {
      return Stream.of(String.format("Field %s must be present", field));
    } else {
      return validators.stream().flatMap(v -> v.validate(tValueField).stream());
    }
  }

  public static <T> Supplier<Stream<String>> rule(T data, String field, List<ValidationFunction<T>> validators) {
    return () -> isNull(data) ? Stream.empty() : executeRule(data, field, validators);
  }

  public static <T> Supplier<Stream<String>> requiredRule(T data, String field, List<ValidationFunction<T>> validators) {
    return () -> executeRule(data, field, validators);
  }
}
