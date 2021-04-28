package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.properties.RabbitProps;
import com.rabbitmq.client.AMQP;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class RabbitSender {

	private final Sender sender;
	private final ObjectMapper objectMapper;
	private final RabbitProps rabbitProps;

	public Mono<OutputMessage> send(OutputMessage msg) {
		if (StringUtils.isEmpty(msg.getData().getResult()) && Objects.isNull(msg.getData().getBinaryFileDto())) {
			return Mono.just(msg);
		} else if (msg.getData().isBinaryFile()) {
			return sendBinary(rabbitProps.getBinaryQueue(), msg.getData()).map(__ -> msg);
		} else {
			return send(rabbitProps.getOutputQueue(), msg.getData()).map(__ -> msg);
		}
	}

	private Mono<BotOutputMessage> send(String key, BotOutputMessage dto) {
		Mono<OutboundMessage> publisher = stringify(dto)
				.map(str -> new OutboundMessage("", key, str.getBytes()));
		return sender.send(publisher).then(Mono.just(dto));
	}

	private Mono<BotOutputMessage> sendBinary(String key, BotOutputMessage dto) {
		Map<String, Object> headers = Map.of(
				"fileName", dto.getBinaryFileDto().fullName(),
				"chatId", dto.getChatId(),
				"msgId", dto.getMsgId()
		);
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
				.headers(headers)
				.build();
		Mono<OutboundMessage> publisher = stringify(dto)
				.map(str -> new OutboundMessage("", key, props, str.getBytes()));
		return sender.send(publisher).then(Mono.just(dto));
	}

	public Mono<String> send(String key, String dto) {
		Mono<OutboundMessage> publisher = Mono.just(new OutboundMessage("", key, dto.getBytes()));
		return sender.send(publisher).then(Mono.just(dto));
	}


	private Mono<String> stringify(BotOutputMessage value) {
		return Mono.fromCallable(() -> objectMapper.writeValueAsString(value));
	}
}
