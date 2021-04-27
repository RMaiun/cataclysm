package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.DumpDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.service.ExportService;
import com.mairo.cataclysm.utils.DateUtils;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DumpCmdProcessor implements CommandProcessor {
	private final ObjectMapper mapper;
	private final ExportService exportService;

	@Override
	public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
		return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), DumpDto.class))
				.flatMap(dto -> exportService.export(DateUtils.now(), dto.getModerator()))
				.map(binaryFileDto -> OutputMessage.ok(BotOutputMessage.asBinary(input.getChatId(), msgId, binaryFileDto)));
	}

	@Override
	public List<String> commands() {
		return Collections.singletonList(DUMP_CMD);
	}
}
