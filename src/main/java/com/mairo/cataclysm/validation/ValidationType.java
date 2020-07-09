package com.mairo.cataclysm.validation;

public interface ValidationType<T> {
  ValidationSchema describeSchema(T dto);
}
