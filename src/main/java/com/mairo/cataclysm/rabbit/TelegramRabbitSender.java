package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.TelegramResponseDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@RequiredArgsConstructor
public class TelegramRabbitSender {

  private final Sender sender;
  private final ObjectMapper objectMapper;
  private final RabbitProps rabbitProps;

  Mono<OutputMessage> send(OutputMessage msg) {
    String queue = msg.isError() ? rabbitProps.getErrorQueue() : rabbitProps.getOutputQueue();
    return send(queue, msg.getData()).map(__ -> msg);
  }

  private Mono<TelegramResponseDto> send(String key, TelegramResponseDto dto) {
    Mono<OutboundMessage> publisher = stringify(dto)
        .map(str -> new OutboundMessage("", key, str.getBytes()));
    return sender.send(publisher).then(Mono.just(dto));
  }

  public Mono<String> send(String key, String dto) {
    Mono<OutboundMessage> publisher = Mono.just(new OutboundMessage("", key, dto.getBytes()));
    return sender.send(publisher).then(Mono.just(dto));
  }

  private Mono<String> stringify(TelegramResponseDto value) {
    try {
      return Mono.just(objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
