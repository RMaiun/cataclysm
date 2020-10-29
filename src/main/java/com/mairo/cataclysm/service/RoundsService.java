package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;
import static com.mairo.cataclysm.validation.ValidationTypes.addRoundValidationType;
import static com.mairo.cataclysm.validation.ValidationTypes.listLastRoundsValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.PlayerSeasonData;
import com.mairo.cataclysm.dto.PlayerSeasonRoundsData;
import com.mairo.cataclysm.exception.SamePlayersInRoundException;
import com.mairo.cataclysm.helper.RoundServiceHelper;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoundsService {

  private final PlayerService playerService;
  private final SeasonRepository seasonRepository;
  private final RoundRepository roundRepository;
  private final RoundServiceHelper roundServiceHelper;
  private final UserRightsService userRightsService;
  private final ReportStupidCacheService cacheService;

  public Mono<FoundLastRounds> findLastRoundsInSeason(FindLastRoundsDto dto) {
    return validate(dto, listLastRoundsValidationType)
        .flatMap(__ -> playerService.findAllPlayersAsMap())
        .zipWith(seasonRepository.getSeason(dto.getSeason()),
            (players, season) -> Pair.of(season, players))
        .flatMap(t -> preparePlayerSeasonData(t, dto.getQty()))
        .map(roundServiceHelper::transformRounds)
        .map(rounds -> new FoundLastRounds(dto.getSeason(), rounds));
  }

  private Mono<PlayerSeasonRoundsData> preparePlayerSeasonData(Pair<Season, Map<Long, String>> t, int itemQty) {
    return roundRepository.listLastRoundsBySeason(t.getKey().getId(), itemQty)
        .map(rounds -> new PlayerSeasonRoundsData(t.getKey(), t.getValue(), rounds));
  }

  Mono<List<FullRound>> findAllRounds(String seasonName) {
    return playerService.findAllPlayersAsMap()
        .zipWith(seasonRepository.getSeason(seasonName),
            (players, season) -> new PlayerSeasonData(season, players))
        .flatMap(psd -> roundRepository.listRoundsBySeason(psd.getSeason().getId())
            .map(rounds -> new PlayerSeasonRoundsData(psd.getSeason(), psd.getPlayers(), rounds)))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    return validate(dto, addRoundValidationType)
        .flatMap(__ -> userRightsService.checkUserIsAdmin(dto.getModerator()))
        .flatMap(__ -> checkAllPlayersAreDifferent(dto).then(checkPlayersExist(dto)))
        .flatMap(p -> saveWithCacheRefresh(p, dto))
        .map(t -> new IdDto(t.getKey()));
  }

  private Mono<Pair<Season, List<Player>>> checkPlayersExist(AddRoundDto dto) {
    return Mono.zip(
        seasonRepository.getSeason(currentSeason()),
        playerService.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())))
        .map(x -> Pair.of(x.getT1(), x.getT2()));
  }

  private Mono<Pair<Long, Optional<BinaryFileDto>>> saveWithCacheRefresh(Pair<Season, List<Player>> p, AddRoundDto dto) {
    return roundRepository.saveRound(roundServiceHelper.prepareRound(p.getRight(), dto, p.getKey().getId()))
        .zipWith(cacheService.remove(p.getKey().getName()), Pair::of);
  }

  private Mono<Void> checkAllPlayersAreDifferent(AddRoundDto dto) {
    List<String> playersList = List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2());
    Set<String> playersSet = new HashSet<>(playersList);
    if (playersList.size() != playersSet.size()) {
      return Mono.error(new SamePlayersInRoundException());
    }
    return Mono.empty();
  }
}
