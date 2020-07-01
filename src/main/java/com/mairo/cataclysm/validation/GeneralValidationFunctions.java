package com.mairo.cataclysm.validation;

import java.util.Collections;
import java.util.Objects;

public interface GeneralValidationFunctions {

  static <T> ValidationFunction<T> isPresent() {
    return vf -> Objects.nonNull(vf.getData())
        ? Collections.emptyList()
        : Collections.singletonList(String.format("Field %s must be present", vf.getField()));
  }
}
