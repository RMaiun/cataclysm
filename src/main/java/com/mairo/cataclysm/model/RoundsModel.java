package com.mairo.cataclysm.model;

import static com.mairo.cataclysm.utils.SeasonUtils.currentSeason;
import static com.mairo.cataclysm.validation.ValidationTypes.addRoundValidationType;
import static com.mairo.cataclysm.validation.ValidationTypes.listLastRoundsValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.PlayerSeasonRoundsData;
import com.mairo.cataclysm.exception.SamePlayersInRoundException;
import com.mairo.cataclysm.helper.RoundServiceHelper;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.service.ReportStupidCacheService;
import com.mairo.cataclysm.service.UserRightsService;
import com.mairo.cataclysm.utils.DateUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        .then(seasonModel.findSeason(dto.getSeason()))
        .then(playerModel.findAllPlayers())
        .flatMap(foundAllPlayers -> preparePlayerSeasonData(foundAllPlayers.getPlayers(), dto.getSeason(), dto.getQty()))
        .map(roundServiceHelper::transformRounds)
        .map(rounds -> new FoundLastRounds(dto.getSeason(), rounds));
  }

  private Mono<PlayerSeasonRoundsData> preparePlayerSeasonData(List<Player> players, String season, int itemQty) {
    List<String> playerNames = players.stream()
        .map(Player::getSurname)
        .collect(Collectors.toList());
    return roundRepository.listLastRoundsBySeason(season, itemQty)
        .map(rounds -> new PlayerSeasonRoundsData(season, playerNames, rounds));
  }

  public Mono<List<FullRound>> findAllRounds(String seasonName) {
    return seasonModel.findSeason(seasonName)
        .then(Mono.zip(
            playerModel.findAllPlayerNames(),
            roundRepository.listRoundsBySeason(seasonName),
            (players, rounds) -> new PlayerSeasonRoundsData(seasonName, players, rounds)))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    return validate(dto, addRoundValidationType)
        .then(userRightsService.checkUserIsAdmin(dto.getModerator()))
        .then(checkAllPlayersAreDifferent(dto))
        .then(checkPlayersExist(dto))
        .then(seasonModel.findSeason(currentSeason()))
        .flatMap(s -> saveWithCacheRefresh(s.getName(), dto))
        .map(r -> new IdDto(r.getId()));
  }

  private Mono<Tuple2<Season, List<Player>>> checkPlayersExist(AddRoundDto dto) {
    return Mono.zip(seasonModel.findSeasonSafely(currentSeason()), playerModel.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())));
  }

  private Mono<Round> saveWithCacheRefresh(String season, AddRoundDto dto) {
    return roundRepository.saveRound(new Round(null, dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2(), dto.isShutout(), season, DateUtils.now()))
        .zipWith(cacheService.remove(season), (r, f) -> r);
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
