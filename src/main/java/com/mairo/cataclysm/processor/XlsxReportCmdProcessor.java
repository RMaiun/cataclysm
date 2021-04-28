package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.*;
import com.mairo.cataclysm.service.ReportGeneratorService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
				// .flatMap(dto -> Mono.fromCallable(this::test))
				.map(binaryFileDto -> OutputMessage.ok(BotOutputMessage.asBinary(input.getChatId(), msgId, binaryFileDto)));
	}

	public BinaryFileDto test() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("C:\\hobby\\cataclysm\\src\\main\\resources\\logback.xml"));
		return new BinaryFileDto(bytes, "logback", "xml");
	}

	@Override
	public List<String> commands() {
		return Collections.singletonList(XLSX_REPORT_CMD);
	}
}
