package com.mairo.cataclysm.rabbit.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.formatter.StoredIdMessageFormatter;
import com.mairo.cataclysm.service.RoundsService;
import com.mairo.cataclysm.utils.ErrorFormatter;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddRoundCmdProcessor {

  private final ObjectMapper mapper;
  private final RoundsService roundsService;
  private final StoredIdMessageFormatter formatter;

  public Mono<OutputMessage> addPlayer(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), AddRoundDto.class))
        .flatMap(roundsService::saveRound)
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)))
        .onErrorResume(e -> Mono.just(OutputMessage.error(new BotOutputMessage(input.getChatId(), msgId, ErrorFormatter.format(e)))));

  }

}
