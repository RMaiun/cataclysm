package com.mairo.cataclysm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindLastRoundsDto {

  private String season;
  private Integer qty;
}
