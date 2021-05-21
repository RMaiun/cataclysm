package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.core.dto.AddPlayerDto;
import com.mairo.cataclysm.core.dto.IdDto;
import com.mairo.cataclysm.core.utils.MonoSupport;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.core.service.PlayerService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddPlayerCmdProcessor implements CommandProcessor {

  private final PlayerService playerService;
  private final ObjectMapper mapper;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), AddPlayerDto.class))
        .flatMap(playerService::addPlayer)
        .map(this::format)
        .map(str -> OutputMessage.ok(input.getChatId(), msgId, str));
  }

  @Override
  public List<String> commands() {
    return Collections.singletonList(ADD_PLAYER_CMD);
  }

  private String format(IdDto data) {
    return String.format("%s New player was stored with id %s %s",
        PREFIX, data.getId(), SUFFIX);
  }
}
