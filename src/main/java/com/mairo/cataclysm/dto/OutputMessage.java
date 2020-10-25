package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputMessage {

  private boolean error;
  private TelegramResponseDto data;

  public static OutputMessage ok(TelegramResponseDto data) {
    return new OutputMessage(false, data);
  }

  public static OutputMessage error(TelegramResponseDto data) {
    return new OutputMessage(true, data);
  }
}
