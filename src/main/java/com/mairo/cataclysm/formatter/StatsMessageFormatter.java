package com.mairo.cataclysm.formatter;

import com.mairo.cataclysm.dto.SeasonShortStats;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class StatsMessageFormatter implements MessageFormatter<SeasonShortStats> {

  @Override
  public String format(SeasonShortStats data) {
    if (data.getGamesPlayed() == 0) {
      return String.format("%sNo games found in season %s%s", PREFIX, data.getSeason(), SUFFIX);
    }

    String ratings = data.getPlayersRating().isEmpty()
        ? "Nobody played more than 30 games"
        : IntStream.range(0, data.getPlayersRating().size())
            .mapToObj(i -> String.format("%d. %s %s", i + 1,
                StringUtils.capitalize(data.getPlayersRating().get(i).getSurname()),
                data.getPlayersRating().get(i).getScore()))
            .collect(Collectors.joining(LINE_SEPARATOR));

    String bestStreak = String.format("%s: %d games in row", data.getBestStreak().getPlayer(), data.getBestStreak().getGames());
    String worstStreak = String.format("%s: %d games in row", data.getWorstStreak().getPlayer(), data.getWorstStreak().getGames());
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
