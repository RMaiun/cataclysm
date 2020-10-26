package com.mairo.cataclysm.rabbit;

import com.mairo.cataclysm.config.properties.RabbitProps;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.util.retry.RetrySpec;

// @Service
@RequiredArgsConstructor
public class OutputTestReceiver {

  private final ConnectionFactory connectionFactory;
  private final RabbitProps rabbitProps;

  @PostConstruct
  public void init() {
    ReceiverOptions receiverOptions = new ReceiverOptions()
        .connectionFactory(connectionFactory)
        .connectionMonoConfigurator(cm -> cm.retryWhen(RetrySpec.backoff(3, Duration.ofSeconds(5))))
        .connectionSupplier(cf -> cf.newConnection("testOutputConn"));

    Receiver receiver = RabbitFlux.createReceiver(receiverOptions);

    receiver.consumeAutoAck(rabbitProps.getOutputQueue())
        .subscribe(delivery -> System.out.println(String.format("OUTPUT: %s",new String(delivery.getBody()))));
  }
}
