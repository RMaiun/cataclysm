package com.mairo.cataclysm.controller;

import static com.mairo.cataclysm.validation.ValidationTypes.addPlayerValidationType;
import static com.mairo.cataclysm.validation.Validator.validate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.dto.InputMessage;
import com.mairo.cataclysm.rabbit.TelegramRabbitSender;
import com.mairo.cataclysm.service.PlayerService;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("players")
@RequiredArgsConstructor
public class PlayerController {

  private final PlayerService playerService;
  private final TelegramRabbitSender sender;
  private final ObjectMapper mapper;

  // @PostConstruct
  //   // public void init() throws JsonProcessingException {
  //   //   IntStream.rangeClosed(1, 10_000)
  //   //       .forEach(i -> {
  //   //         try {
  //   //           sender.send("input_q", mapper.writeValueAsString(new InputMessage("" + i, "xyz", new HashMap<>())))
  //   //               .subscribe();
  //   //         } catch (JsonProcessingException e) {
  //   //           e.printStackTrace();
  //   //         }
  //   //       });
  //   // }

  @GetMapping("/all")
  public Mono<FoundAllPlayers> findAllPlayers() {
    return playerService.findAllPlayers()
        .flatMap(x ->
        {
          try {
            return sender.send("input_q", mapper.writeValueAsString(new InputMessage("111", "xyz", new HashMap<>())))
                .map(om -> Pair.of(x, om));
          } catch (JsonProcessingException e) {
            return Mono.just(Pair.of(x, "Sdas"));
          }
        }).map(Pair::getLeft);
  }

  @PostMapping("/add")
  public Mono<IdDto> addPlayer(@RequestBody AddPlayerDto addPlayerDto) {
    return validate(addPlayerDto, addPlayerValidationType)
        .flatMap(playerService::addPlayer);
  }
}
