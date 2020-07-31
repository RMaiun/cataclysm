package com.mairo.cataclysm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

  @Id
  private Long id;

  private String surname;
  private String tid;
  private boolean admin;
}
