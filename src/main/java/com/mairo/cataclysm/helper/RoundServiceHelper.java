package com.mairo.cataclysm.helper;

import static java.util.stream.Collectors.toList;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerSeasonRoundsData;
import com.mairo.cataclysm.utils.DateUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
