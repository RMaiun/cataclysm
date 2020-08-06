package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.PlayerSeasonData;
import com.mairo.cataclysm.exception.SamePlayersInRoundException;
import com.mairo.cataclysm.helper.RoundServiceHelper;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class RoundsService {

  private final PlayerService playerService;
  private final SeasonRepository seasonRepository;
  private final RoundRepository roundRepository;
  private final RoundServiceHelper roundServiceHelper;
  private final UserRightsService userRightsService;
  private final ReportStupidCacheService cacheService;

  public Mono<List<FullRound>> findLastRoundsInSeason(FindLastRoundsDto dto) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(dto.getSeason()),
            (players, season) -> new PlayerSeasonData(season, players, null))
        .flatMap(psd -> roundRepository.listLastRoundsBySeason(psd.getSeason().getId(), dto.getQty()).map(psd::withRounds))
        .map(roundServiceHelper::transformRounds);
  }

  Mono<List<FullRound>> findAllRounds(String seasonName) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players, null))
        .flatMap(psd -> roundRepository.listRoundsBySeason(psd.getSeason().getId()).map(psd::withRounds))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    Mono<Void> differentPlayersMono = checkAllPlayersAreDifferent(dto);
    return userRightsService.checkUserIsAdmin(dto.getModerator())
        .flatMap(__ -> differentPlayersMono.then(checkPlayersExist(dto)))
        .flatMap(t -> saveWithCacheRefresh(t, dto))
        .map(t -> new IdDto(t.getT1()));
  }

  private Mono<Tuple2<Season, List<Player>>> checkPlayersExist(AddRoundDto dto){
    return  Mono.zip(
        seasonRepository.getSeason(currentSeason()),
        playerService.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())));
  }

  private Mono<Tuple2<Long, Optional<BinaryFileDto>>> saveWithCacheRefresh(Tuple2<Season, List<Player>> t, AddRoundDto dto) {
    return Mono.zip(
        roundRepository.saveRound(roundServiceHelper.prepareRound(t.getT2(), dto, t.getT1().getId())),
        cacheService.remove(t.getT1().getName()));
  }

  private Mono<Void> checkAllPlayersAreDifferent(AddRoundDto dto) {
    List<String> playersList = List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2());
    Set<String> playersSet = new HashSet<>(playersList);
    if (playersList.size() != playersSet.size()) {
      return Mono.error(new SamePlayersInRoundException());
    }
    return Mono.empty().then();
  }
}
