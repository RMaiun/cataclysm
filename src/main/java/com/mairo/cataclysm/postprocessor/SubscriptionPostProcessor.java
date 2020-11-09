package com.mairo.cataclysm.postprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.LinkTidDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.formatter.MessageFormatter;
import com.mairo.cataclysm.rabbit.RabbitSender;
import com.mairo.cataclysm.utils.MonoSupport;
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
  public List<String> cmds() {
    return Collections.singletonList("linkTid");
  }

  @Override
  public Flux<OutputMessage> postProcess(BotInputMessage input, int msgId) {
    String message = String.format("%s You was participated for notifications%s", MessageFormatter.PREFIX, MessageFormatter.SUFFIX);
    Mono<OutputMessage> res = MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), LinkTidDto.class))
        .map(dto -> new BotOutputMessage(dto.getTid(), msgId, message))
        .map(OutputMessage::ok)
        .flatMap(data -> rabbitSender.send(data).map(__ -> data));
    return Flux.from(res);
  }
}
