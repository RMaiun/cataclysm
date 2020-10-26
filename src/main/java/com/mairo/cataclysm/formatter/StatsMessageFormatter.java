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
      return String.format("No games found in season %s", data.getSeason());
    }
    String ratings = IntStream.range(0, data.getPlayersRating().size())
        .mapToObj(i -> String.format("%d. %s %d", i + 1,
            StringUtils.capitalize(data.getPlayersRating().get(i).getSurname()),
            data.getPlayersRating().get(i).getScore())
        ).collect(Collectors.joining(System.lineSeparator()));

    String bestStreak = String.format("%s: %d games in row", data.getBestStreak().getPlayer(), data.getBestStreak().getGames());
    String worstStreak = String.format("%s: %d games in row", data.getWorstStreak().getPlayer(), data.getWorstStreak().getGames());
    String separator = StringUtils.repeat("-", 30);
    return "```"
        + "Season: " + data.getSeason() + System.lineSeparator()
        + "Games played: " + data.getGamesPlayed() + System.lineSeparator()
        + "Days till season end: " + data.getDaysToSeasonEnd() + System.lineSeparator()
        + separator + System.lineSeparator()
        + "Current Rating:" + System.lineSeparator()
        + ratings + System.lineSeparator()
        + separator + System.lineSeparator()
        + "Best Streak:" + System.lineSeparator()
        + bestStreak + System.lineSeparator()
        + separator + System.lineSeparator()
        + "Worst Streak:" + System.lineSeparator()
        + worstStreak + System.lineSeparator()
        + "```";
  }
}
