package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerSeasonData;
import com.mairo.cataclysm.dto.api.FindAllRoundsRequest;
import com.mairo.cataclysm.dto.api.FindAllRoundsResponse;
import com.mairo.cataclysm.dto.api.FindLastRoundsRequest;
import com.mairo.cataclysm.dto.api.FindLastRoundsResponse;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class RoundsService {

  private final PlayerService playerService;
  private final SeasonRepository seasonRepository;
  private final RoundRepository roundRepository;

  public RoundsService(PlayerService playerService, SeasonRepository seasonRepository, RoundRepository roundRepository) {
    this.playerService = playerService;
    this.seasonRepository = seasonRepository;
    this.roundRepository = roundRepository;
  }

  public Mono<FindAllRoundsResponse> findAllRoundsInSeason(FindAllRoundsRequest dto) {
    return findARoundsInSeason(dto.getSeason(), Integer.MAX_VALUE)
        .map(this::prepareGameStats);
  }

  public Mono<FindLastRoundsResponse> findLastRoundsInSeason(FindLastRoundsRequest dto) {
    return findARoundsInSeason(dto.getSeason(), dto.getQty())
        .map(FindLastRoundsResponse::new);

  }

  private Mono<List<FullRound>> findARoundsInSeason(String seasonName, int qty) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players))
        .flatMap(psd -> findAllRounds(psd.getSeason(), psd.getPlayers()));
  }

  private Mono<List<FullRound>> findAllRounds(Season s, Map<Long, String> playersMap) {
    return roundRepository.listRoundsBySeason(s.getId())
        .map(data -> data.stream()
            .map(r -> transformRound(r, playersMap, s.getName()))
            .collect(Collectors.toList()));
  }

  private FullRound transformRound(Round r, Map<Long, String> pm, String s) {
    return new FullRound(
        StringUtils.capitalize(pm.get(r.getWinner1())),
        StringUtils.capitalize(pm.get(r.getWinner2())),
        StringUtils.capitalize(pm.get(r.getLoser1())),
        StringUtils.capitalize(pm.get(r.getLoser2())),
        r.getCreated(),
        s, r.isShutout());
  }

  private FindAllRoundsResponse prepareGameStats(List<FullRound> rounds) {
    List<String> headers = rounds.stream()
        .flatMap(r -> Stream.of(r.getWinner1(), r.getWinner2(), r.getLoser1(), r.getLoser2()))
        .distinct()
        .sorted()
        .collect(Collectors.toList());

    Map<String, Integer> points = rounds.stream()
        .flatMap(r -> Stream.of(r.getWinner1(), r.getWinner2(), r.getLoser1(), r.getLoser2()))
        .distinct()
        .collect(Collectors.toMap(x -> x, x -> 1000));
    rounds.forEach(r -> {
      int wp = r.isShutout() ? 50 : 25;
      int lp = r.isShutout() ? -50 : -25;
      points.computeIfPresent(r.getWinner1(), (k, v) -> v + wp);
      points.computeIfPresent(r.getWinner2(), (k, v) -> v + wp);
      points.computeIfPresent(r.getLoser1(), (k, v) -> v + lp);
      points.computeIfPresent(r.getLoser2(), (k, v) -> v + lp);
    });

    List<Integer> totals = points.entrySet().stream()
        .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
        .collect(Collectors.toList());

    List<List<String>> games = rounds.stream().map(r -> {
      List<String> row = IntStream.range(0, headers.size()).mapToObj(x -> "").collect(Collectors.toList());
      String winPoints = r.isShutout() ? "50" : "25";
      String losePoints = r.isShutout() ? "-50" : "-25";
      row.set(headers.indexOf(r.getWinner1()), winPoints);
      row.set(headers.indexOf(r.getWinner2()), winPoints);
      row.set(headers.indexOf(r.getLoser1()), losePoints);
      row.set(headers.indexOf(r.getLoser2()), losePoints);
      return row;
    }).collect(Collectors.toList());

    return new FindAllRoundsResponse(headers, totals, games, rounds.size());
  }
}
