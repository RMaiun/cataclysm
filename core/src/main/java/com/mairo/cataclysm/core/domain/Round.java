package com.mairo.cataclysm.core.domain;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Round {

  @Id
  private String id;
  private String winner1;
  private String winner2;
  private String loser1;
  private String loser2;
  private boolean shutout;
  private String season;
  private ZonedDateTime created;
}
