package com.mairo.cataclysm.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ErrorDto {

  private String msg;
  private String useCase;
  private LocalDateTime timestamp = LocalDateTime.now();

  public ErrorDto(String msg, String useCase) {
    this.msg = msg;
    this.useCase = useCase;
  }
}
