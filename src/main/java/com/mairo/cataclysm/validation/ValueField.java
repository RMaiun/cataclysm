package com.mairo.cataclysm.validation;

public class ValueField<T> {

  private T data;
  private String field;

  public ValueField(T data, String field) {
    this.data = data;
    this.field = field;
  }

  public T getData() {
    return data;
  }

  public String getField() {
    return field;
  }

}
