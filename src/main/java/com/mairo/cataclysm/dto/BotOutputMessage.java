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

}
