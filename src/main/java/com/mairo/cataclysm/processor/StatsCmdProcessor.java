package com.mairo.cataclysm.processor;

import static org.apache.commons.lang3.StringUtils.capitalize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.properties.AppProps;
import com.mairo.cataclysm.service.StatisticsService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatsCmdProcessor implements CommandProcessor {

  private final ObjectMapper mapper;
  private final StatisticsService statisticsService;
  private final AppProps appProps;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), SeasonShortStats.class))
        .flatMap(dto -> statisticsService.seasonShortInfoStatistics(dto.getSeason()))
        .map(this::format)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, str));
  }

  @Override
  public List<String> commands() {
    return List.of(SHORT_STATS_CMD);
  }

  private String format(SeasonShortStats data) {
    if (data.getGamesPlayed() == 0) {
      return String.format("%sNo games found in season %s%s", PREFIX, data.getSeason(), SUFFIX);
    }

    String ratings = data.getPlayersRating().isEmpty()
        ? String.format("Nobody played more than %d games", appProps.getExpectedGames())
        : IntStream.range(0, data.getPlayersRating().size())
            .mapToObj(i -> String.format("%d. %s %s", i + 1,
                capitalize(data.getPlayersRating().get(i).getSurname()),
                data.getPlayersRating().get(i).getScore()))
            .collect(Collectors.joining(LINE_SEPARATOR));

    String bestStreak = String.format("%s: %d games in row", capitalize(data.getBestStreak().getPlayer()), data.getBestStreak().getGames());
    String worstStreak = String.format("%s: %d games in row", capitalize(data.getWorstStreak().getPlayer()), data.getWorstStreak().getGames());
    String separator = StringUtils.repeat("-", 30);
    return PREFIX
        + "Season: " + data.getSeason() + LINE_SEPARATOR
        + "Games played: " + data.getGamesPlayed() + LINE_SEPARATOR
        + "Days till season end: " + data.getDaysToSeasonEnd() + LINE_SEPARATOR
        + separator + LINE_SEPARATOR
        + "Current Rating:" + LINE_SEPARATOR
        + ratings + LINE_SEPARATOR
        + separator + LINE_SEPARATOR
        + "Best Streak:" + LINE_SEPARATOR
        + bestStreak + LINE_SEPARATOR
        + separator + LINE_SEPARATOR
        + "Worst Streak:" + LINE_SEPARATOR
        + worstStreak + LINE_SEPARATOR
        + SUFFIX;
  }
}
