package com.mairo.cataclysm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.config.properties.RabbitProps;
import com.mairo.cataclysm.rabbit.RabbitSender;
import com.rabbitmq.client.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.ChannelPool;
import reactor.rabbitmq.ChannelPoolFactory;
import reactor.rabbitmq.ChannelPoolOptions;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

@Configuration
@RequiredArgsConstructor
public class RabbitConfiguration {

  private final RabbitProps rabbitProperties;
  private final ObjectMapper objectMapper;

  // @PreDestroy
  // public void close(ConnectionFactory rabbitConnectionFactory) throws Exception {
  //   Objects.requireNonNull(rabbitConnectionFactory.).close();
  // }

  @Bean()
  ConnectionFactory rabbitConnectionFactory() {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(rabbitProperties.getHost());
    connectionFactory.setPort(rabbitProperties.getPort());
    connectionFactory.setUsername(rabbitProperties.getUsername());
    connectionFactory.setPassword(rabbitProperties.getPassword());
    connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
    connectionFactory.useNio();
    return connectionFactory;
  }

  @Bean
  Sender sender(ConnectionFactory connectionFactory) {
    SenderOptions senderOptions = new SenderOptions()
        .connectionFactory(connectionFactory)
        .connectionSupplier(cf -> cf.newConnection("sender"))
        .resourceManagementScheduler(Schedulers.elastic());

    Sender sender = RabbitFlux.createSender(senderOptions);
    sender.declare(QueueSpecification.queue(rabbitProperties.getInputQueue()))
        .then(sender.declare(QueueSpecification.queue(rabbitProperties.getOutputQueue())))
        .then(sender.declare(QueueSpecification.queue(rabbitProperties.getErrorQueue())))
        .then(sender.declare(QueueSpecification.queue(rabbitProperties.getBinaryQueue())))
        .subscribe();
    return sender;
  }

  @Bean
  RabbitSender rabbitSender(Sender sender) {
    return new RabbitSender(sender, objectMapper, rabbitProperties);
  }

  @Bean
  ChannelPool channelPool(ConnectionFactory connectionFactory) {
    return ChannelPoolFactory.createChannelPool(
        Mono.fromCallable(connectionFactory::newConnection).cache(),
        new ChannelPoolOptions().maxCacheSize(10)
    );
  }


}
