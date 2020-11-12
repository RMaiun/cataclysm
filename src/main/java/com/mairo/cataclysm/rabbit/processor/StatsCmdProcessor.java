package com.mairo.cataclysm.rabbit.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.dto.SeasonShortStats;
import com.mairo.cataclysm.formatter.StatsMessageFormatter;
import com.mairo.cataclysm.service.StatisticsService;
import com.mairo.cataclysm.utils.MonoSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatsCmdProcessor {

  private final ObjectMapper mapper;
  private final StatisticsService statisticsService;
  private final StatsMessageFormatter formatter;

  public Mono<OutputMessage> prepareStats(BotInputMessage input, int msgId) {
    return MonoSupport.fromTry(() -> mapper.convertValue(input.getData(), SeasonShortStats.class))
        .flatMap(dto -> statisticsService.seasonShortInfoStatistics(dto.getSeason()))
        .map(formatter::format)
        .map(str -> OutputMessage.ok(new BotOutputMessage(input.getChatId(), msgId, str)));
  }
}
