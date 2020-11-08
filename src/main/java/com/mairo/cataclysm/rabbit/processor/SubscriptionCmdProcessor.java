package com.mairo.cataclysm.rabbit.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.SubscriptionActionDto;
import com.mairo.cataclysm.dto.SubscriptionResultDto;
import com.mairo.cataclysm.formatter.MessageFormatter;
import com.mairo.cataclysm.service.SubscriptionService;
import com.mairo.cataclysm.utils.ErrorFormatter;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionCmdProcessor {

  private final ObjectMapper objectMapper;
  private final SubscriptionService subscriptionService;

  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> objectMapper.convertValue(input.getData(), SubscriptionActionDto.class))
        .flatMap(subscriptionService::updateSubscriptionsStatus)
        .map(this::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)))
        .onErrorResume(e -> Mono.just(OutputMessage.error(new BotOutputMessage(input.getChatId(), msgId, ErrorFormatter.format(e)))));
  }

  private String format(SubscriptionResultDto dto) {
    String action = dto.isNotificationsEnabled() ? "enabled" : "disabled";
    return String.format("%s Notifications were %s%s", MessageFormatter.PREFIX, action, MessageFormatter.SUFFIX);
  }
}
