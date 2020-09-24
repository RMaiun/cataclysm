package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.RabbitErrorAdapterDto;
import com.mairo.cataclysm.service.PlayerService;
import com.rabbitmq.client.ConnectionFactory;
import io.vavr.control.Try;
import java.time.Duration;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.util.retry.RetrySpec;

@Service("listPlayersReceiver")
public class ListPlayersReceiver extends GeneralReceiver<FoundAllPlayers> {

  private final PlayerService playerService;
  private final ConnectionFactory connectionFactory;
  private final ObjectMapper objectMapper;

  public ListPlayersReceiver(RabbitSender rabbitSender, RabbitProps rabbitProps, PlayerService playerService, ConnectionFactory connectionFactory,
      ObjectMapper objectMapper) {
    super(rabbitSender, rabbitProps);
    this.playerService = playerService;
    this.connectionFactory = connectionFactory;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    ReceiverOptions receiverOptions = new ReceiverOptions()
        .connectionFactory(connectionFactory)
        .connectionMonoConfigurator(cm -> cm.retryWhen(RetrySpec.backoff(3, Duration.ofSeconds(5))))
        .connectionSupplier(cf -> cf.newConnection("sender"));
    Receiver receiver = RabbitFlux.createReceiver(receiverOptions);
    receiver.consumeAutoAck(rabbitProps.getListPlayersQueue().getName())
        .flatMap(delivery -> deser(delivery.getBody()))
        .map(RabbitErrorAdapterDto::new)
        .onErrorResume(this::handleError)
        .subscribe(System.out::println);
  }

  private Mono<FoundAllPlayers> deser(byte[] data) {
    return Mono.fromCallable(() -> Try.of(() -> objectMapper.readValue(data, FoundAllPlayers.class)))
        .flatMap(x -> x.isFailure() ? Mono.error(x.getCause()) : Mono.just(x.get()));
  }

}
