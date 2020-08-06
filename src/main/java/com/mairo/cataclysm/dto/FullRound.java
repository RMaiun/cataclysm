package com.mairo.cataclysm.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private boolean shutout;
}
