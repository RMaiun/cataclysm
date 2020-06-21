package com.mairo.cataclysm.service;

import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.PlayerSeasonData;
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

  public Mono<List<FullRound>> findLastRoundsInSeason(String seasonName, Integer qty) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players, null))
        .flatMap(psd -> roundRepository.listLastRoundsBySeason(psd.getSeason().getId(), qty).map(psd::withRounds))
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
    return seasonRepository.getSeason(currentSeason())
        .flatMap(s -> roundRepository.saveRound(new Round(null,
            dto.getW1(), dto.getW2(),
            dto.getL1(), dto.getL2(),
            dto.isShutout(),
            s.getId(),
            LocalDateTime.now(ZoneOffset.UTC))))
        .map(IdDto::new);
  }
}
