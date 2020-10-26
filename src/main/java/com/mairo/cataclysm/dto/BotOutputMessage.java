package com.mairo.cataclysm.dto;


public class BotOutputMessage {

  private String chatId;
  private int msgId;
  private String data;

  public BotOutputMessage(String chatId, int msgId, String data) {
    this.chatId = chatId;
    this.msgId = msgId;
    this.data = data;
  }

  public int getMsgId() {
    return msgId;
  }

  public void setMsgId(int msgId) {
    this.msgId = msgId;
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
