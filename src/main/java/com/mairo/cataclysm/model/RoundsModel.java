package com.mairo.cataclysm.model;

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
import com.mairo.cataclysm.service.ReportStupidCacheService;
import com.mairo.cataclysm.service.UserRightsService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class RoundsModel {

  private final PlayerModel playerModel;
  private final SeasonModel seasonModel;
  private final RoundRepository roundRepository;
  private final RoundServiceHelper roundServiceHelper;
  private final UserRightsService userRightsService;
  private final ReportStupidCacheService cacheService;

  public Mono<FoundLastRounds> findLastRoundsInSeason(FindLastRoundsDto dto) {
    return validate(dto, listLastRoundsValidationType)
        .then(Mono.zip(playerModel.findAllPlayersAsMap(), seasonModel.findSeason(dto.getSeason())))
        .flatMap(t -> preparePlayerSeasonData(t, dto.getQty()))
        .map(roundServiceHelper::transformRounds)
        .map(rounds -> new FoundLastRounds(dto.getSeason(), rounds));
  }

  private Mono<PlayerSeasonRoundsData> preparePlayerSeasonData(Tuple2<Map<Long, String>, Season> t, int itemQty) {
    return roundRepository.listLastRoundsBySeason(t.getT2().getId(), itemQty)
        .map(rounds -> new PlayerSeasonRoundsData(t.getT2(), t.getT1(), rounds));
  }

  public Mono<List<FullRound>> findAllRounds(String seasonName) {
    return Mono.zip(seasonModel.findSeason(seasonName), playerModel.findAllPlayersAsMap(), PlayerSeasonData::new)
        .flatMap(psd -> roundRepository.listRoundsBySeason(psd.getSeason().getId())
            .map(rounds -> new PlayerSeasonRoundsData(psd.getSeason(), psd.getPlayers(), rounds)))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    return validate(dto, addRoundValidationType)
        .then(userRightsService.checkUserIsAdmin(dto.getModerator()))
        .then(checkAllPlayersAreDifferent(dto))
        .then(checkPlayersExist(dto))
        .flatMap(p -> saveWithCacheRefresh(p, dto))
        .map(t -> new IdDto(t.getKey()));
  }

  private Mono<Tuple2<Season, List<Player>>> checkPlayersExist(AddRoundDto dto) {
    return Mono.zip(seasonModel.findSeasonSafely(currentSeason()), playerModel.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())));
  }

  private Mono<Pair<Long, Optional<BinaryFileDto>>> saveWithCacheRefresh(Tuple2<Season, List<Player>> p, AddRoundDto dto) {
    return roundRepository.saveRound(roundServiceHelper.prepareRound(p.getT2(), dto, p.getT1().getId()))
        .zipWith(cacheService.remove(p.getT1().getName()), Pair::of);
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
