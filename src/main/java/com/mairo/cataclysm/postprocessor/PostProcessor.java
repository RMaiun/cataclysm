package com.mairo.cataclysm.postprocessor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import reactor.core.publisher.Flux;

public interface PostProcessor {

  String cmd();

  Flux<OutputMessage> postProcess(BotInputMessage input, int msgId);
}
