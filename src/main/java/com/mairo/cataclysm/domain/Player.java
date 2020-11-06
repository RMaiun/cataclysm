package com.mairo.cataclysm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class Player {

  @Id
  private Long id;

  private String surname;
  private String tid;
  private boolean admin;
  @Column(value = "enable_notifications")
  private boolean notificationsEnabled;
}
