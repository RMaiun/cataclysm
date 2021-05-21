package com.mairo.cataclysm.core.service;

import static com.mairo.cataclysm.core.utils.SeasonUtils.currentSeason;
import static com.mairo.cataclysm.core.validation.ValidationTypes.addRoundValidationType;
import static com.mairo.cataclysm.core.validation.ValidationTypes.listLastRoundsValidationType;
import static com.mairo.cataclysm.core.validation.Validator.validate;

import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.domain.Round;
import com.mairo.cataclysm.core.domain.Season;
import com.mairo.cataclysm.core.dto.AddRoundDto;
import com.mairo.cataclysm.core.dto.FindLastRoundsDto;
import com.mairo.cataclysm.core.dto.FoundLastRounds;
import com.mairo.cataclysm.core.dto.FullRound;
import com.mairo.cataclysm.core.dto.IdDto;
import com.mairo.cataclysm.core.dto.PlayerSeasonRoundsData;
import com.mairo.cataclysm.core.exception.SamePlayersInRoundException;
import com.mairo.cataclysm.core.helper.RoundServiceHelper;
import com.mairo.cataclysm.core.repository.RoundRepository;
import com.mairo.cataclysm.core.utils.DateUtils;
import com.mairo.cataclysm.core.utils.SeasonUtils;
import com.mairo.cataclysm.core.validation.ValidationTypes;
import com.mairo.cataclysm.core.validation.Validator;
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
public class RoundsService {

  private final PlayerService playerService;
  private final SeasonService seasonService;
  private final RoundRepository roundRepository;
  private final RoundServiceHelper roundServiceHelper;
  private final UserRightsService userRightsService;
  private final ReportStupidCacheService cacheService;

  public Mono<FoundLastRounds> findLastRoundsInSeason(FindLastRoundsDto dto) {
    return Validator.validate(dto, ValidationTypes.listLastRoundsValidationType)
        .then(seasonService.findSeason(dto.getSeason()))
        .then(playerService.findAllPlayers())
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
    return seasonService.findSeason(seasonName)
        .then(Mono.zip(
            playerService.findAllPlayerNames(),
            roundRepository.listRoundsBySeason(seasonName),
            (players, rounds) -> new PlayerSeasonRoundsData(seasonName, players, rounds)))
        .map(roundServiceHelper::transformRounds);
  }

  public Mono<IdDto> saveRound(AddRoundDto dto) {
    return Validator.validate(dto, ValidationTypes.addRoundValidationType)
        .then(userRightsService.checkUserIsAdmin(dto.getModerator()))
        .then(checkAllPlayersAreDifferent(dto))
        .then(checkPlayersExist(dto))
        .then(seasonService.findSeason(SeasonUtils.currentSeason()))
        .flatMap(s -> saveWithCacheRefresh(s.getName(), dto))
        .map(r -> new IdDto(r.getId()));
  }

  private Mono<Tuple2<Season, List<Player>>> checkPlayersExist(AddRoundDto dto) {
    return Mono.zip(seasonService.findSeasonSafely(SeasonUtils.currentSeason()), playerService.checkPlayersExist(List.of(dto.getW1(), dto.getW2(), dto.getL1(), dto.getL2())));
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
