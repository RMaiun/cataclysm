package com.mairo.cataclysm.postprocessor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.utils.Commands;
import reactor.core.publisher.Flux;

public interface PostProcessor extends Commands {

  Flux<OutputMessage> postProcess(BotInputMessage input, int msgId);
}
