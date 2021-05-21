package com.mairo.cataclysm.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Document
public class Player {

  @Id
  private String id;

  private String surname;
  private String tid;
  private boolean admin;
  private boolean notificationsEnabled;
}
