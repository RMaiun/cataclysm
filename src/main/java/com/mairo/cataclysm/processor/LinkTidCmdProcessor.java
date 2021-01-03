package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.LinkTidDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.SubscriptionResultDto;
import com.mairo.cataclysm.service.SubscriptionService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LinkTidCmdProcessor implements CommandProcessor {

  @Override
  public List<String> commands() {
    return List.of(LINK_TID_CMD);
  }

  private final ObjectMapper mapper;
  private final SubscriptionService subscriptionService;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), LinkTidDto.class))
        .flatMap(subscriptionService::linkTidForPlayer)
        .map(this::format)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, str));
  }

  private String format(SubscriptionResultDto data) {
    return String.format("%s Notifications were linked for %s%s", PREFIX, data.getSubscribedSurname(), SUFFIX);
  }

}
