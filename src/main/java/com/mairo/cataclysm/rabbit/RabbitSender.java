package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.properties.RabbitProps;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@RequiredArgsConstructor
public class RabbitSender {

  private final Sender sender;
  private final ObjectMapper objectMapper;
  private final RabbitProps rabbitProps;

  public Mono<OutputMessage> send(OutputMessage msg) {
    String queue = msg.isError() ? rabbitProps.getErrorQueue() : rabbitProps.getOutputQueue();
    return send(queue, msg.getData()).map(__ -> msg);
  }

  private Mono<BotOutputMessage> send(String key, BotOutputMessage dto) {
    Mono<OutboundMessage> publisher = stringify(dto)
        .map(str -> new OutboundMessage("", key, str.getBytes()));
    return sender.send(publisher).then(Mono.just(dto));
  }

  public Mono<String> send(String key, String dto) {
    Mono<OutboundMessage> publisher = Mono.just(new OutboundMessage("", key, dto.getBytes()));
    return sender.send(publisher).then(Mono.just(dto));
  }

  private Mono<String> stringify(BotOutputMessage value) {
    try {
      return Mono.just(objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
