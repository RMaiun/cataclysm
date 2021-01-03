package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.SubscriptionActionDto;
import com.mairo.cataclysm.dto.SubscriptionResultDto;
import com.mairo.cataclysm.service.SubscriptionService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionCmdProcessor implements CommandProcessor {

  private final ObjectMapper objectMapper;
  private final SubscriptionService subscriptionService;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> objectMapper.convertValue(input.getData(), SubscriptionActionDto.class))
        .flatMap(subscriptionService::updateSubscriptionsStatus)
        .map(this::format)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, str));
  }

  @Override
  public List<String> commands() {
    return List.of(SUBSCRIBE_CMD, UNSUBSCRIBE_CMD);
  }

  private String format(SubscriptionResultDto dto) {
    String action = dto.isNotificationsEnabled() ? "enabled" : "disabled";
    return String.format("%s Notifications were %s%s", PREFIX, action, SUFFIX);
  }
}
