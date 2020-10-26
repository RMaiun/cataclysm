package com.mairo.cataclysm.rabbit;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.exception.InvalidCommandException;
import com.mairo.cataclysm.rabbit.processor.AddRoundCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.LastCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.ListPlayersCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.StatsCmdProcessor;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.util.retry.RetrySpec;

@RequiredArgsConstructor
@Service
public class CommandReceiver {

  private final MetadataParser metadataParser;
  private final RabbitSender rabbitSender;
  private final ConnectionFactory connectionFactory;
  private final RabbitProps rabbitProps;

  private final ListPlayersCmdProcessor listPlayersCmdProcessor;
  private final AddRoundCmdProcessor addRoundCmdProcessor;
  private final StatsCmdProcessor statsCmdProcessor;
  private final LastCmdProcessor lastCmdProcessor;

  @PostConstruct
  public void init() {
    ReceiverOptions receiverOptions = new ReceiverOptions()
        .connectionFactory(connectionFactory)
        .connectionMonoConfigurator(cm -> cm.retryWhen(RetrySpec.backoff(3, Duration.ofSeconds(3))))
        .connectionSupplier(cf -> cf.newConnection("listPlayersConn"));

    Receiver receiver = RabbitFlux.createReceiver(receiverOptions);

    receiver.consumeAutoAck(rabbitProps.getInputQueue())
        .flatMap(delivery -> metadataParser.parseCommand(delivery.getBody()))
        .flatMap(this::processCmd)
        .flatMap(rabbitSender::send)
        .subscribe(System.out::println);
  }

  private Mono<OutputMessage> processCmd(BotInputMessage dto) {
    return Match(dto.getCmd()).of(
        Case($("listPlayers"), listPlayersCmdProcessor.preparePlayers(dto, msgId())),
        Case($("addPlayer"), addRoundCmdProcessor.addPlayer(dto, msgId())),
        Case($("shortStats"), statsCmdProcessor.prepareStats(dto, msgId())),
        Case($("findLastRounds"), lastCmdProcessor.prepareStats(dto, msgId())),
        Case($(), Mono.error(new InvalidCommandException(dto.getCmd())))
    );
  }

  private int msgId() {
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }
}
