package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.dto.*;
import com.mairo.cataclysm.helper.RoundServiceHelper;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;

@Service
@RequiredArgsConstructor
public class RoundsService {

  private final PlayerService playerService;
  private final SeasonRepository seasonRepository;
  private final RoundRepository roundRepository;
  private final RoundServiceHelper roundServiceHelper;

  public Mono<List<FullRound>> findLastRoundsInSeason(FindLastRoundsDto dto) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(dto.getSeason()),
            (players, season) -> new PlayerSeasonData(season, players, null))
        .flatMap(psd -> roundRepository.listLastRoundsBySeason(psd.getSeason().getId(), dto.getQty()).map(psd::withRounds))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<List<FullRound>> findAllRounds(String seasonName) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players, null))
        .flatMap(psd -> roundRepository.listRoundsBySeason(psd.getSeason().getId()).map(psd::withRounds))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    return Mono.zip(
        seasonRepository.getSeason(currentSeason()),
        playerService.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())))
        .flatMap(t -> roundRepository.saveRound(new Round(null,
            dto.getW1(), dto.getW2(),
            dto.getL1(), dto.getL2(),
            dto.isShutout(),
            t.getT1().getId(),
            LocalDateTime.now(ZoneOffset.UTC))))
        .map(IdDto::new);
  }
}
