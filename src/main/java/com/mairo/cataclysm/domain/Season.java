package com.mairo.cataclysm.domain;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season {

  @Id
  private String id;
  private String name;
  private ZonedDateTime seasonEndNotification;

  public static Season of(String name) {
    return new Season(null, name, null);
  }

  public static Season of(String name, ZonedDateTime seasonEndNotification) {
    return new Season(null, name, seasonEndNotification);
  }
}
