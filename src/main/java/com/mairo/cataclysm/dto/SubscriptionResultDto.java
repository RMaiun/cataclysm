package com.mairo.cataclysm.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResultDto {

  private String subscribedSurname;
  private String subscribedTid;
  private LocalDateTime createdDate;
  private boolean notificationsEnabled;
}
