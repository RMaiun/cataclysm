package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputMessage {

  private boolean error;
  private BotOutputMessage data;

  public static OutputMessage ok(String chatId, int msgId, String result) {
    return new OutputMessage(false, new BotOutputMessage(chatId, msgId, result));
  }

  public static OutputMessage ok(BotOutputMessage botOutputMessage) {
    return new OutputMessage(false, botOutputMessage);
  }

  public static OutputMessage error(String chatId, int msgId, String result) {
    return new OutputMessage(true, new BotOutputMessage(chatId, msgId, result));
  }

  public static OutputMessage error(BotOutputMessage botOutputMessage) {
    return new OutputMessage(true, botOutputMessage);
  }
}
