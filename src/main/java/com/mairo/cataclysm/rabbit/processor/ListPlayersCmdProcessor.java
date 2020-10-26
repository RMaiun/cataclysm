package com.mairo.cataclysm.rabbit.processor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.formatter.ListPlayersMessageFormatter;
import com.mairo.cataclysm.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ListPlayersCmdProcessor {

  private final PlayerService playerService;
  private final ListPlayersMessageFormatter formatter;


  public Mono<OutputMessage> preparePlayers(BotInputMessage dto, int msgId) {
    return playerService.findAllPlayers()
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(dto.getChatId(), msgId, str)))
        .onErrorResume(e -> Mono.just(OutputMessage.error(new BotOutputMessage(dto.getChatId(), msgId, e.getMessage()))));
  }

}
