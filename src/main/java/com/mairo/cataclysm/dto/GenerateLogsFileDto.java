package com.mairo.cataclysm.dto;

import java.time.ZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateLogsFileDto {

  private ZonedDateTime from;
  private ZonedDateTime to;
}
