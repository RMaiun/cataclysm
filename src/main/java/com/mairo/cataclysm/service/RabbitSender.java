package com.mairo.cataclysm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.properties.RabbitProps;
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
    if (msg.getData().getResult().isEmpty()) {
      return Mono.just(msg);
    } else {
      return send(rabbitProps.getOutputQueue(), msg.getData()).map(__ -> msg);
    }
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
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(value));
  }
}
