package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@RequiredArgsConstructor
public class RabbitSender {

  private final Sender sender;
  private final String exchange;
  private final ObjectMapper objectMapper;


  public <T> Mono<T> send(String key, T value) {
    Mono<OutboundMessage> publisher = stringify(value)
        .map(str -> new OutboundMessage(exchange, key, str.getBytes()));
    return sender.send(publisher).then(Mono.just(value));
  }

  private <T> Mono<String> stringify(T value) {
    try {
      return Mono.just(objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
