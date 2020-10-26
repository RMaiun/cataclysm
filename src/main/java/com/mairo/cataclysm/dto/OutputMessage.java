package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputMessage {

  private boolean error;
  private BotOutputMessage data;

  public static OutputMessage ok(BotOutputMessage data) {
    return new OutputMessage(false, data);
  }

  public static OutputMessage error(BotOutputMessage data) {
    return new OutputMessage(true, data);
  }
}
