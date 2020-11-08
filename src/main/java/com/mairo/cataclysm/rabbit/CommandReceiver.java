package com.mairo.cataclysm.rabbit;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.exception.InvalidCommandException;
import com.mairo.cataclysm.postprocessor.PostProcessor;
import com.mairo.cataclysm.properties.RabbitProps;
import com.mairo.cataclysm.rabbit.processor.AddRoundCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.LastCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.LinkTidCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.ListPlayersCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.StatsCmdProcessor;
import com.mairo.cataclysm.rabbit.processor.SubscriptionCmdProcessor;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
  private final LinkTidCmdProcessor linkTidCmdProcessor;
  private final SubscriptionCmdProcessor subscriptionCmdProcessor;

  private final List<PostProcessor> postProcessorList;

  @PostConstruct
  public void init() {
    ReceiverOptions receiverOptions = new ReceiverOptions()
        .connectionFactory(connectionFactory)
        .connectionMonoConfigurator(cm -> cm.retryWhen(RetrySpec.backoff(3, Duration.ofSeconds(3))))
        .connectionSupplier(cf -> cf.newConnection("cata_input_receiver_conn"));

    Receiver receiver = RabbitFlux.createReceiver(receiverOptions);

    receiver.consumeAutoAck(rabbitProps.getInputQueue())
        .flatMap(delivery -> metadataParser.parseCommand(delivery.getBody()))
        .flatMap(input -> processCmd(input).map(res -> Pair.of(input, res)))
        .flatMap(p -> rabbitSender.send(p.getRight()).map(__ -> p))
        .flatMap(p -> postProcess(p.getLeft()))
        .subscribe();
  }

  private Flux<OutputMessage> postProcess(BotInputMessage input) {
    return postProcessorList.stream()
        .filter(pp -> pp.cmds().contains(input.getCmd()))
        .findAny()
        .map(pp -> pp.postProcess(input, msgId()))
        .orElseGet(Flux::empty);
  }

  private Mono<OutputMessage> processCmd(BotInputMessage dto) {
    return Match(dto.getCmd()).of(
        Case($("listPlayers"), listPlayersCmdProcessor.preparePlayers(dto, msgId())),
        Case($("addRound"), addRoundCmdProcessor.addPlayer(dto, msgId())),
        Case($("shortStats"), statsCmdProcessor.prepareStats(dto, msgId())),
        Case($("findLastRounds"), lastCmdProcessor.prepareStats(dto, msgId())),
        Case($("linkTid"), linkTidCmdProcessor.process(dto, msgId())),
        Case($("subscribe"), subscriptionCmdProcessor.process(dto, msgId())),
        Case($("unsubscribe"), subscriptionCmdProcessor.process(dto, msgId())),
        Case($(), Mono.error(new InvalidCommandException(dto.getCmd())))
    );
  }

  private int msgId() {
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }
}
