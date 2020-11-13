package com.mairo.cataclysm.postprocessor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import java.util.List;
import reactor.core.publisher.Flux;

public interface PostProcessor {

  List<String> commands();

  Flux<OutputMessage> postProcess(BotInputMessage input, int msgId);
}
