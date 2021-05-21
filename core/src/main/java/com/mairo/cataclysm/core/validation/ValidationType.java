package com.mairo.cataclysm.core.validation;

public interface ValidationType<T> {
  ValidationSchema applyDto(T dto);
}
