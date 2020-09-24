package com.mairo.cataclysm.dto;

import lombok.ToString;

@ToString
public class RabbitErrorAdapterDto<T> {

  private T dto;
  private Throwable err;

  public RabbitErrorAdapterDto(T dto) {
    this.dto = dto;
  }

  public RabbitErrorAdapterDto(Throwable err) {
    this.err = err;
  }
}
