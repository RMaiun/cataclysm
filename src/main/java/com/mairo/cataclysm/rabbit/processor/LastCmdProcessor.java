package com.mairo.cataclysm.rabbit.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.formatter.LastRoundsMessageFormatter;
import com.mairo.cataclysm.service.RoundsService;
import com.mairo.cataclysm.utils.ErrorFormatter;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LastCmdProcessor {

  private final ObjectMapper mapper;
  private final RoundsService roundsService;
  private final LastRoundsMessageFormatter formatter;

  public Mono<OutputMessage> prepareStats(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), FindLastRoundsDto.class))
        .flatMap(roundsService::findLastRoundsInSeason)
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)));
  }
}
