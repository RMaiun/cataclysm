package com.mairo.cataclysm.dto;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportDumpData {

  List<Season> seasonList = new ArrayList<>();
  List<Player> playersList = new ArrayList<>();
  List<Round> roundsList = new ArrayList<>();
}
