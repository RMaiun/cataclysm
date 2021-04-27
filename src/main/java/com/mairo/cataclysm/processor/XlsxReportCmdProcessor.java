package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.*;
import com.mairo.cataclysm.service.ReportGeneratorService;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XlsxReportCmdProcessor implements CommandProcessor {
	private final ObjectMapper mapper;
	private final ReportGeneratorService reportGeneratorService;

	@Override
	public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
		return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), XlsxReportDto.class))
				.flatMap(dto -> reportGeneratorService.generateXslxReport(new GenerateStatsDocumentDto(dto.getSeason())))
				.map(binaryFileDto -> OutputMessage.ok(BotOutputMessage.asBinary(input.getChatId(), msgId, binaryFileDto)));
	}

	@Override
	public List<String> commands() {
		return Collections.singletonList(XLSX_REPORT_CMD);
	}
}
