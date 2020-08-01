package com.mairo.cataclysm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.mairo.cataclysm.TestData;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.exception.CataRuntimeException;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import com.mairo.cataclysm.service.UserRightsService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoundsControllerTest {

  @Autowired
  private ApplicationContext ctx;
  @MockBean
  PlayerRepository playerRepository;
  @MockBean
  SeasonRepository seasonRepository;
  @MockBean
  RoundRepository roundRepository;
  @MockBean
  UserRightsService userRightsService;

  private static WebTestClient webClient;

  @BeforeEach
  void setup() {
    webClient = WebTestClient.bindToApplicationContext(ctx).build();
    when(playerRepository.listAll()).thenReturn(Mono.just(TestData.testPlayers()));
    when(seasonRepository.getSeason(any())).thenReturn(Mono.just(new Season(1L, "S1|2020")));
    when(roundRepository.listLastRoundsBySeason(anyLong(), anyInt())).thenReturn(Mono.just(TestData.testRounds()));
  }

  @Test
  @DisplayName("rounds/findLast test")
  void findLastTest() {
    webClient.get()
        .uri("/rounds/findLast/S1|2020/10")
        .exchange()
        .expectStatus().isOk()
        .expectBody(FoundLastRounds.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(4, res.getResponseBody().getRounds().size());
        });
  }

  @Test
  @DisplayName("rounds/add")
  void addRoundHdsTest() {
    when(roundRepository.saveRound(any())).thenReturn(Mono.just(100L));
    when(playerRepository.findPlayers(anyList())).thenAnswer(invocation -> {
      List<String> argument = invocation.getArgument(0);
      return Mono.just(TestData.players(argument));
    });
    when(userRightsService.checkUserIsAdmin(eq("1111"))).thenReturn(Mono.just(new Player()));

    webClient.post()
        .uri("/rounds/add")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new AddRoundDto("px", "py", "pz", "pk", true, "1111")))
        .exchange()
        .expectStatus().isOk()
        .expectBody(IdDto.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(100L, res.getResponseBody().getId());
        });
  }

  @Test
  @DisplayName("rounds/add with absent players")
  void addRoundNoPlayersFoundTest() {
    when(roundRepository.saveRound(any())).thenReturn(Mono.just(100L));
    when(playerRepository.findPlayers(anyList())).thenAnswer(invocation -> {
      List<String> argument = invocation.getArgument(0);
      return Mono.just(TestData.players(argument.subList(0, argument.size() - 2)));
    });
    when(userRightsService.checkUserIsAdmin(eq("1111"))).thenReturn(Mono.just(new Player()));

    webClient.post()
        .uri("/rounds/add")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new AddRoundDto("px", "py", "pz", "pk", true, "1111")))
        .exchange()
        .expectStatus().is4xxClientError()
        .expectBody(CataRuntimeException.class);
  }

  @Test
  @DisplayName("rounds/add with same players")
  void addRoundWithSamePlayersTest() {
    when(roundRepository.saveRound(any())).thenReturn(Mono.just(100L));
    when(playerRepository.findPlayers(anyList())).thenAnswer(invocation -> {
      List<String> argument = invocation.getArgument(0);
      return Mono.just(TestData.players(argument.subList(0, argument.size() - 2)));
    });
    when(userRightsService.checkUserIsAdmin(eq("1111"))).thenReturn(Mono.just(new Player()));

    webClient.post()
        .uri("/rounds/add")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new AddRoundDto("px", "px", "pz", "pk", true, "1111")))
        .exchange()
        .expectStatus().is4xxClientError()
        .expectBody(CataRuntimeException.class);
  }
}
