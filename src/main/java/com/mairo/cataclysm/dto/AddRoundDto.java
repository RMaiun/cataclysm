package com.mairo.cataclysm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddRoundDto {

  private String w1;
  private String w2;
  private String l1;
  private String l2;
  private boolean shutout;
}
