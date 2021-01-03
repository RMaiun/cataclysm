package com.mairo.cataclysm.processor;

import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.utils.Commands;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public interface CommandProcessor extends Commands {

  String LINE_SEPARATOR = System.lineSeparator();
  String DELIMITER = StringUtils.repeat("-", 34) + LINE_SEPARATOR;
  String SUFFIX = "```";
  String PREFIX = SUFFIX + LINE_SEPARATOR;

  Mono<OutputMessage> process(BotInputMessage input, int msgId);

  List<String> commands();
}
