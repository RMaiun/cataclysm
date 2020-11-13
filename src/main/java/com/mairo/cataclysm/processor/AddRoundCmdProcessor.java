package com.mairo.cataclysm.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.service.RoundsService;
import com.mairo.cataclysm.utils.MonoSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddRoundCmdProcessor implements CommandProcessor {

  private static final String ADD_ROUND_CMD = "addRound";

  private final ObjectMapper mapper;
  private final RoundsService roundsService;

  @Override
  public Mono<OutputMessage> process(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), AddRoundDto.class))
        .flatMap(roundsService::saveRound)
        .map(this::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)));
  }

  @Override
  public List<String> commands() {
    return List.of(ADD_ROUND_CMD);
  }

  private String format(IdDto data) {
    return String.format("%s New round was stored with id %s %s",
        PREFIX, data.getId(), SUFFIX);
  }
}
