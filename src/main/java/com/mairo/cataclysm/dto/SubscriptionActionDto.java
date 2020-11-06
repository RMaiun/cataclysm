package com.mairo.cataclysm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionActionDto {

  private boolean enableSubscriptions;
  private String tid;
}
