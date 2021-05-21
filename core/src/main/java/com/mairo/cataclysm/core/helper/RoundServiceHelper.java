package com.mairo.cataclysm.core.helper;

import static java.util.stream.Collectors.toList;

import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.domain.Round;
import com.mairo.cataclysm.core.dto.FullRound;
import com.mairo.cataclysm.core.dto.PlayerSeasonRoundsData;
import com.mairo.cataclysm.core.utils.DateUtils;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoundServiceHelper {

  private FullRound transformRound(Round r, String s) {
    return new FullRound(
        r.getWinner1(),
        r.getWinner2(),
        r.getLoser1(),
        r.getLoser2(),
        DateUtils.utcToEet(r.getCreated()),
        s, r.isShutout());
  }

  public List<FullRound> transformRounds(PlayerSeasonRoundsData psd) {
    return psd.getRounds().stream()
        .map(r -> transformRound(r, psd.getSeason()))
        .sorted(Comparator.comparing(FullRound::getCreated))
        .collect(toList());
  }

}
