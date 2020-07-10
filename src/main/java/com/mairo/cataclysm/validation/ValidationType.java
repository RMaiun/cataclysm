package com.mairo.cataclysm.validation;

public interface ValidationType<T> {
  ValidationSchema applyDto(T dto);
}
