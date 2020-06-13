package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.PlayerSeasonData;
import com.mairo.cataclysm.dto.api.FindAllSeasonRoundsRequest;
import com.mairo.cataclysm.dto.api.FindAllSeasonRoundsResponse;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoundService {

  private final PlayerRepository playerRepository;
  private final SeasonRepository seasonRepository;
  private final RoundRepository roundRepository;

  public RoundService(PlayerRepository playerRepository, SeasonRepository seasonRepository, RoundRepository roundRepository) {
    this.playerRepository = playerRepository;
    this.seasonRepository = seasonRepository;
    this.roundRepository = roundRepository;
  }

  public Mono<Either<Throwable, FindAllSeasonRoundsResponse>> findAllRoundsInSeason(FindAllSeasonRoundsRequest dto) {
    return playerRepository.listAll()
        .zipWith(seasonRepository.getSeason(dto.getSeason()),
            (players, maybeSeason) -> new PlayerSeasonData(maybeSeason, players))
        .flatMap(this::prepareRounds);
  }

  private Mono<Either<Throwable, FindAllSeasonRoundsResponse>> prepareRounds(PlayerSeasonData psd) {
    return psd.getSeason().fold(
        () -> Mono.just(Either.left(new RuntimeException("Season is not found"))),
        s -> findAllRounds(s, psd.getPlayers()).map(Either::right)
    );
  }

  private Mono<FindAllSeasonRoundsResponse> findAllRounds(Season s, List<Player> players) {
    Map<Long, String> playersMap = players.stream().collect(Collectors.toMap(Player::getId, Player::getSurname));
    return roundRepository.listLastRoundsBySeason(10, s.getId())
        .map(data -> data.stream()
            .map(r -> transformRound(r, playersMap, s.getName()))
            .collect(Collectors.toList()))
        .map(FindAllSeasonRoundsResponse::new);
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
}
