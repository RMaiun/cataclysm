package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.TelegramResponseDto;
import com.mairo.cataclysm.formatter.ListPlayersMessageFormatter;
import com.mairo.cataclysm.service.PlayerService;
import com.rabbitmq.client.ConnectionFactory;
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
  private final ListPlayersMessageFormatter formatter;

  public ListPlayersReceiver(TelegramRabbitSender telegramRabbitSender, RabbitProps rabbitProps, ConnectionFactory connectionFactory,
      ObjectMapper objectMapper, PlayerService playerService, ListPlayersMessageFormatter formatter) {
    super(telegramRabbitSender, rabbitProps, objectMapper);
    this.connectionFactory = connectionFactory;
    this.playerService = playerService;
    this.formatter = formatter;
  }

  @PostConstruct
  public void init() {
    ReceiverOptions receiverOptions = new ReceiverOptions()
        .connectionFactory(connectionFactory)
        .connectionMonoConfigurator(cm -> cm.retryWhen(RetrySpec.backoff(3, Duration.ofSeconds(5))))
        .connectionSupplier(cf -> cf.newConnection("listPlayersConn"));

    Receiver receiver = RabbitFlux.createReceiver(receiverOptions);

    receiver.consumeAutoAck(rabbitProps.getInputQueue())
        .flatMap(delivery -> deserialize(delivery.getBody()))
        .flatMap(x -> preparePlayers(x.getChatId()))
        .flatMap(telegramRabbitSender::send)
        .subscribe(System.out::println);
  }

  private Mono<OutputMessage> preparePlayers(String chatId) {
    return playerService.findAllPlayers()
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new TelegramResponseDto(chatId, str)))
        .onErrorResume(e -> Mono.just(OutputMessage.error(new TelegramResponseDto(chatId, e.getMessage()))));
  }

}
