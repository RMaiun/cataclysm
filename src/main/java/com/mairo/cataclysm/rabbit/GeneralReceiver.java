package com.mairo.cataclysm.rabbit;

import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.RabbitErrorAdapterDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class GeneralReceiver<T> {
  protected final RabbitSender rabbitSender;
  protected final RabbitProps rabbitProps;

  protected Mono<RabbitErrorAdapterDto<T>> handleError(Throwable t){
    return rabbitSender.send(rabbitProps.getErrorsQueue().getKey(), t.getMessage())
        .then(Mono.just(new RabbitErrorAdapterDto<T>(t)));
  }
}
