package com.mairo.cataclysm.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MetadataParser {

  private final ObjectMapper mapper;


  Mono<BotInputMessage> parseCommand(byte[] body) {
    return MonoSupport.fromTry(() -> mapper.readValue(body, BotInputMessage.class));
  }
}
