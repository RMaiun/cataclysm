package com.mairo.cataclysm.postprocessor;

import static com.mairo.cataclysm.core.utils.MonoSupport.fromTry;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.capitalize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.core.domain.Player;
import com.mairo.cataclysm.core.dto.AddRoundDto;
import com.mairo.cataclysm.dto.BotInputMessage;
import com.mairo.cataclysm.dto.BotOutputMessage;
import com.mairo.cataclysm.dto.OutputMessage;
import com.mairo.cataclysm.processor.CommandProcessor;
import com.mairo.cataclysm.service.RabbitSender;
import com.mairo.cataclysm.core.service.PlayerService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AddRoundPostProcessor implements PostProcessor {

  private final ObjectMapper mapper;
  private final PlayerService playerService;
  private final RabbitSender rabbitSender;

  @Override
  public List<String> commands() {
    return Collections.singletonList(ADD_ROUND_CMD);
  }

  @Override
  public Flux<OutputMessage> postProcess(BotInputMessage input, int msgId) {
    Mono<AddRoundDto> parsedDto = parse(input.getData());
    Mono<List<Triple<String, String, Boolean>>> dataList = parsedDto.map(dto -> List.of(
        Triple.of(dto.getW1(), format("%s/%s", capitalize(dto.getL1()), capitalize(dto.getL2())), true),
        Triple.of(dto.getW2(), format("%s/%s", capitalize(dto.getL1()), capitalize(dto.getL2())), true),
        Triple.of(dto.getL1(), format("%s/%s", capitalize(dto.getW1()), capitalize(dto.getW2())), false),
        Triple.of(dto.getL2(), format("%s/%s", capitalize(dto.getW1()), capitalize(dto.getW2())), false)
    ));

    return dataList.flatMapMany(Flux::fromIterable)
        .flatMap(el -> sendNotificationToUser(msgId, el.getLeft(), el.getRight(), el.getMiddle()));
  }

  private Mono<AddRoundDto> parse(Map<String, Object> data) {
    return fromTry(() -> mapper.convertValue(data, AddRoundDto.class));
  }

  private Mono<OutputMessage> sendNotificationToUser(int msgId, String player, boolean winner, String opponents) {
    return playerService.findPlayerByName(player)
        .filter(Player::isNotificationsEnabled)
        .filter(p -> nonNull(p.getTid()))
        .map(p -> new BotOutputMessage(p.getTid(), msgId, formatNotification(opponents, winner)))
        .map(OutputMessage::ok)
        .flatMap(dto -> rabbitSender.send(dto).map(__ -> dto));
  }

  private String formatNotification(String opponents, boolean winner) {
    String action = winner ? "WIN" : "LOSE";
    return format("%sYour %s against %s was stored%s",
        CommandProcessor.PREFIX, action, capitalize(opponents), CommandProcessor.SUFFIX);
  }
}
