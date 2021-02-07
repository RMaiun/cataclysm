package com.mairo.cataclysm.processor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.utils.Commands;
import java.util.List;
import reactor.core.publisher.Mono;

public interface CommandProcessor extends Commands {

  Mono<OutputMessage> process(BotInputMessage input, int msgId);

  List<String> commands();
}
