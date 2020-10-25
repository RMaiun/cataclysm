package com.mairo.cataclysm.dto;


public class TelegramResponseDto {

  private String chatId;
  private String data;

  public TelegramResponseDto(String chatId, String data) {
    this.chatId = chatId;
    this.data = data;
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
