package com.mairo.cataclysm.helper;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerSeasonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class RoundServiceHelper {

  public FullRound transformRound(Round r, Map<Long, String> pm, String s) {
    return new FullRound(
        StringUtils.capitalize(pm.get(r.getWinner1())),
        StringUtils.capitalize(pm.get(r.getWinner2())),
        StringUtils.capitalize(pm.get(r.getLoser1())),
        StringUtils.capitalize(pm.get(r.getLoser2())),
        r.getCreated(),
        s, r.isShutout());
  }

  public List<FullRound> transformRounds(PlayerSeasonData psd) {
    return psd.getRounds().stream()
        .map(r -> transformRound(r, psd.getPlayers(), psd.getSeason().getName()))
        .collect(toList());
  }
}
