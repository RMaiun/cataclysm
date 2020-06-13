package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullRound {
  private String winner1;
  private String winner2;
  private String loser1;
  private String loser2;
  private LocalDateTime created;
  private String season;
  private boolean isShutout;
}
