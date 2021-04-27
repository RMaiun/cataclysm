package com.mairo.cataclysm.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotOutputMessage {

  private String chatId;
  private int msgId;
  private String result;
  private boolean binaryFile;
  private BinaryFileDto binaryFileDto;

  public static BotOutputMessage asString(String chatId, int msgId, String result){
    return new BotOutputMessage(chatId, msgId, result, false, null);
  }

  public static BotOutputMessage asBinary(String chatId, int msgId, BinaryFileDto result){
    return new BotOutputMessage(chatId, msgId, null, true, result);
  }
}
