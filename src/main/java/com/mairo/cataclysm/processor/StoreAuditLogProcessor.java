package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.StoreAuditLogDto;
import com.mairo.cataclysm.service.AuditLogService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StoreAuditLogProcessor implements CommandProcessor {

  private final ObjectMapper mapper;
  private final AuditLogService auditLogService;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), StoreAuditLogDto.class))
        .flatMap(auditLogService::storeAuditLog)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, DEFAULT_RESULT));
  }

  @Override
  public List<String> commands() {
    return Collections.singletonList(LOG_CMD);
  }
}
