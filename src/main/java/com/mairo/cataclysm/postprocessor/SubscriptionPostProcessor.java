package com.mairo.cataclysm.postprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.core.dto.LinkTidDto;
import com.mairo.cataclysm.core.utils.MonoSupport;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.processor.CommandProcessor;
import com.mairo.cataclysm.service.RabbitSender;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionPostProcessor implements PostProcessor {

  private final ObjectMapper mapper;
  private final RabbitSender rabbitSender;

  @Override
  public List<String> commands() {
    return Collections.singletonList(LINK_TID_CMD);
  }

  @Override
  public Flux<OutputMessage> postProcess(BotInputMessage input, int msgId) {
    String message = String.format("%s You was participated for notifications%s", CommandProcessor.PREFIX, CommandProcessor.SUFFIX);
    Mono<OutputMessage> res = MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), LinkTidDto.class))
        .map(dto -> new BotOutputMessage(dto.getTid(), msgId, message))
        .map(OutputMessage::ok)
        .flatMap(data -> rabbitSender.send(data).map(__ -> data));
    return Flux.from(res);
  }
}
