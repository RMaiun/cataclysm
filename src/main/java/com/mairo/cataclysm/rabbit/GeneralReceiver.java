package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.InputMessage;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class GeneralReceiver<T> {

  protected final TelegramRabbitSender telegramRabbitSender;
  protected final RabbitProps rabbitProps;
  protected final ObjectMapper objectMapper;

  Mono<InputMessage> deserialize(byte[] data) {
    return Mono.fromCallable(() -> Try.of(() -> objectMapper.readValue(data, InputMessage.class)))
        .map(msg -> {
          System.out.println(String.format("INPUT=%s", new String(data)));
          return msg;
        })
        .flatMap(x -> x.isFailure() ? Mono.error(x.getCause()) : Mono.just(x.get()));
  }
}
