package com.mairo.cataclysm.core.dto;

import lombok.Value;

@Value
public class SeasonNotificationData {

  String season;
  boolean readyToBeProcessed;
}
