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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.*;

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
    return findRoundsInSeason(dto.getSeason(), null)
        .map(this::prepareGameStats);
  }

  public Mono<FindLastRoundsResponse> findLastRoundsInSeason(FindLastRoundsRequest dto) {
    return findRoundsInSeason(dto.getSeason(), dto.getQty())
        .map(FindLastRoundsResponse::new);

  }

  private Mono<List<FullRound>> findRoundsInSeason(String seasonName, Integer qty) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players))
        .flatMap(psd -> findAllRounds(psd.getSeason(), psd.getPlayers(), qty));
  }

  private Mono<List<Round>> listData(long sid, Integer qty) {
    if (nonNull(qty)) {
      return roundRepository.listLastRoundsBySeason(sid, qty);
    }
    return roundRepository.listRoundsBySeason(sid);
  }

  private Mono<List<FullRound>> findAllRounds(Season s, Map<Long, String> playersMap, Integer qty) {
    return listData(s.getId(), qty)
        .map(data -> data.stream()
            .map(r -> transformRound(r, playersMap, s.getName()))
            .collect(toList()));
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
        .collect(toList());

    List<Integer> totals = rounds.stream()
        .flatMap(r -> Stream.of(
            Pair.of(r.getWinner1(), r.isShutout() ? 50 : 25),
            Pair.of(r.getWinner2(), r.isShutout() ? 50 : 25),
            Pair.of(r.getLoser1(), r.isShutout() ? -50 : -25),
            Pair.of(r.getLoser2(), r.isShutout() ? -50 : -25)))
        .collect(groupingBy(Pair::getKey, mapping(Pair::getRight, reducing(0, Integer::sum))))
        .entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .map(x -> x + 1000)
        .collect(toList());

    List<List<String>> games = rounds.stream()
        .map(r -> {
          List<String> row = IntStream.range(0, headers.size()).mapToObj(x -> "").collect(toList());
          String winPoints = r.isShutout() ? "50" : "25";
          String losePoints = r.isShutout() ? "-50" : "-25";
          row.set(headers.indexOf(r.getWinner1()), winPoints);
          row.set(headers.indexOf(r.getWinner2()), winPoints);
          row.set(headers.indexOf(r.getLoser1()), losePoints);
          row.set(headers.indexOf(r.getLoser2()), losePoints);
          return row;
        }).collect(toList());

    return new FindAllRoundsResponse(headers, totals, games, rounds.size());
  }
}
