package com.mairo.cataclysm.core.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class ImportDumpDto {

  private int seasons;
  private int players;
  private int auditLogs;
  private Map<String, Long> rounds;
}
