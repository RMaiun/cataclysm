package com.mairo.cataclysm.rabbit.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.LinkTidDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.formatter.LinkTidFormatter;
import com.mairo.cataclysm.service.SubscriptionService;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LinkTidCmdProcessor {

  private final ObjectMapper mapper;
  private final SubscriptionService subscriptionService;
  private final LinkTidFormatter formatter;

  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), LinkTidDto.class))
        .flatMap(subscriptionService::linkTidForPlayer)
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)));
  }

}
