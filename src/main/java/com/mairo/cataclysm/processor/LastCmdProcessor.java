package com.mairo.cataclysm.processor;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.core.dto.FindLastRoundsDto;
import com.mairo.cataclysm.core.dto.FoundLastRounds;
import com.mairo.cataclysm.core.dto.FullRound;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.core.service.RoundsService;
import com.mairo.cataclysm.core.utils.DateUtils;
import com.mairo.cataclysm.core.utils.MonoSupport;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LastCmdProcessor implements CommandProcessor {

  private final ObjectMapper mapper;
  private final RoundsService roundsService;


  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), FindLastRoundsDto.class))
        .flatMap(roundsService::findLastRoundsInSeason)
        .map(this::format)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, str));
  }

  @Override
  public List<String> commands() {
    return List.of(FIND_LAST_ROUNDS_CMD);
  }

  private String format(FoundLastRounds data) {
    if (isEmpty(data.getRounds())) {
      return String.format("%s There are no games in season %s%s", PREFIX, data.getSeason(), SUFFIX);
    } else {
      return data.getRounds().stream()
          .map(this::formatRound)
          .collect(Collectors.joining(DELIMITER, PREFIX, SUFFIX));
    }
  }

  private String formatRound(FullRound round) {
    String date = DateUtils.formatDateWithHour(round.getCreated());
    String winners = String.format("%s/%s", capitalize(round.getWinner1()), capitalize(round.getWinner2()));
    String losers = String.format("%s/%s", capitalize(round.getLoser1()), capitalize(round.getLoser2()));
    StringBuilder sb = new StringBuilder();
    sb.append("date: ").append(date).append(LINE_SEPARATOR);
    sb.append("winners: ").append(winners).append(LINE_SEPARATOR);
    sb.append("losers: ").append(losers).append(LINE_SEPARATOR);
    if (round.isShutout()) {
      sb.append("shutout: ✓").append(LINE_SEPARATOR);
    }
    return sb.toString();
  }
}
