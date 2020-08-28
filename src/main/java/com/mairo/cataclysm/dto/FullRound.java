package com.mairo.cataclysm.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullRound {

  private long w1Id;
  private String winner1;
  private long w2Id;
  private String winner2;
  private long l1Id;
  private String loser1;
  private long l2Id;
  private String loser2;
  private LocalDateTime created;
  private String season;
  private boolean shutout;
}
