package com.mairo.cataclysm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.core.utils.MonoSupport;
import com.mairo.cataclysm.dto.BotInputMessage;
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
