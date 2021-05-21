package com.mairo.cataclysm.core.domain;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

  @Id
  private String id;
  private String msg;
  private ZonedDateTime created;
}
