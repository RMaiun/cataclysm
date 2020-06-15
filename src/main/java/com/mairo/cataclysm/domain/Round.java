package com.mairo.cataclysm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Round {
  @Id
  private Long id;
  @Column(value = "winner1_id")
  private Long winner1;
  @Column(value = "winner2_id")
  private Long winner2;
  @Column(value = "loser1_id")
  private Long loser1;
  @Column(value = "loser2_id")
  private Long loser2;
  private boolean shutout;
  @Column(value = "season_id")
  private Long seasonId;
  private LocalDateTime created;
}
