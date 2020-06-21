package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRoundDto {
  private long w1;
  private long w2;
  private long l1;
  private long l2;
  private boolean shutout;
}
