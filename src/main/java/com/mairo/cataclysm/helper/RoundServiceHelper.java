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

  private FullRound transformRound(Round r, Map<Long, String> pm, String s) {
    return new FullRound(
        r.getWinner1(),
        pm.get(r.getWinner1()),
        r.getWinner2(),
        pm.get(r.getWinner2()),
        r.getLoser1(),
        pm.get(r.getLoser1()),
        r.getLoser2(),
        pm.get(r.getLoser2()),
        DateUtils.utcToEet(r.getCreated()),
        s, r.isShutout());
  }

  public List<FullRound> transformRounds(PlayerSeasonRoundsData psd) {
    return psd.getRounds().stream()
        .map(r -> transformRound(r, psd.getPlayers(), psd.getSeason().getName()))
        .sorted(Comparator.comparing(FullRound::getCreated))
        .collect(toList());
  }

  public Round prepareRound(List<Player> players, AddRoundDto dto, Long seasonId) {
    List<Long> relatedSurnames = findRelatedSurnames(dto, players);
    return new Round(null, relatedSurnames.get(0), relatedSurnames.get(1), relatedSurnames.get(2),
        relatedSurnames.get(3), dto.isShutout(), seasonId, DateUtils.now());
  }

  private List<Long> findRelatedSurnames(AddRoundDto dto, List<Player> foundPlayers) {
    Map<String, Long> players = foundPlayers.stream()
        .collect(Collectors.toMap(Player::getSurname, Player::getId));
    return Arrays.asList(players.get(dto.getW1()), players.get(dto.getW2()),
        players.get(dto.getL1()), players.get(dto.getL2()));
  }
}
