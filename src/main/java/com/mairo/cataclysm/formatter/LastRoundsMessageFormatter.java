package com.mairo.cataclysm.formatter;

import static java.lang.System.lineSeparator;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.FullRound;
import com.mairo.cataclysm.utils.DateUtils;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LastRoundsMessageFormatter implements MessageFormatter<FoundLastRounds> {

  private static final String SEPARATOR = StringUtils.repeat("-", 34);


  @Override
  public String format(FoundLastRounds data) {

    if (isEmpty(data.getRounds())) {
      return String.format("```There are no games in season %s for current moment```", data.getSeason());
    } else {
      return data.getRounds().stream()
          .map(this::formatRound)
          .collect(Collectors.joining(SEPARATOR, "```", "```"));
    }
  }

  private String formatRound(FullRound round) {
    String date = DateUtils.formatDateWithHour(round.getCreated());
    String winners = String.format("%s/%s", round.getWinner1(), round.getWinner2());
    String losers = String.format("%s/%s", round.getLoser1(), round.getLoser2());
    String shutout = round.isShutout() ? String.format("%s|shutout: ✓", lineSeparator()) : "";
    return "date: " + date + lineSeparator()
        + "winners: " + winners + lineSeparator()
        + "losers: " + losers + lineSeparator()
        + shutout;
  }
}
