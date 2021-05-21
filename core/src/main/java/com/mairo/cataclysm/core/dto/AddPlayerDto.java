package com.mairo.cataclysm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPlayerDto {

  private String surname;
  private String tid;
  private boolean admin;
  private String moderator;
}
