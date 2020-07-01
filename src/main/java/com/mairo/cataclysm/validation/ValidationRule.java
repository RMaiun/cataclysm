package com.mairo.cataclysm.validation;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ValidationRule {

  public static <T> List<String> executeRule(T data, String field, ValidationFunction<T>... validators) {
    ValueField<T> tValueField = new ValueField<>(data, field);
    List<String> result = new ArrayList<>();
    if (isNull(data)) {
      result.add(String.format("Field %s must be present", field));
    } else {
      for (ValidationFunction<T> validator : validators) {
        List<String> validationResult = validator.validate(tValueField);
        result.addAll(validationResult);
      }
    }
    return result;
  }

  public static <T> Supplier<List<String>> rule(T data, String field, ValidationFunction<T>... validators) {
    return () -> isNull(data) ? Collections.emptyList() : executeRule(data, field, validators);
  }

  public static <T> Supplier<List<String>> requiredRule(T data, String field, ValidationFunction<T>... validators) {
    return () -> executeRule(data, field, validators);
  }
}
