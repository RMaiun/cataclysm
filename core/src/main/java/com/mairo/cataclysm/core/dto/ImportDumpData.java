package com.mairo.cataclysm.core.dto;

import com.mairo.cataclysm.core.domain.AuditLog;
import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.domain.Round;
import com.mairo.cataclysm.core.domain.Season;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportDumpData {

  private List<Season> seasonList = new ArrayList<>();
  private List<Player> playersList = new ArrayList<>();
  private List<Round> roundsList = new ArrayList<>();
  private List<AuditLog> auditLogList = new ArrayList<>();
}
