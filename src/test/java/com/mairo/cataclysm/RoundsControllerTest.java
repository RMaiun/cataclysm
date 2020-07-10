package com.mairo.cataclysm;

import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FoundLastRounds;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoundsControllerTest {

  @Autowired
  private ApplicationContext ctx;
  @MockBean
  PlayerRepository playerRepository;
  @MockBean
  SeasonRepository seasonRepository;
  @MockBean
  RoundRepository roundRepository;

  private static WebTestClient webClient;

  @BeforeEach
  public void setup() {
    webClient = WebTestClient.bindToApplicationContext(ctx).build();
    when(playerRepository.listAll()).thenReturn(Mono.just(TestData.testPlayers()));
    when(seasonRepository.getSeason(any())).thenReturn(Mono.just(new Season(1L, "S1|2020")));
    when(roundRepository.listLastRoundsBySeason(anyLong(), anyInt())).thenReturn(Mono.just(TestData.testRounds()));
  }

  @Test
  @DisplayName("round/findLast test")
  public void findLastTest() {
    webClient.get()
        .uri("/round/findLast/S1|2020/10")
        .exchange()
        .expectStatus().isOk()
        .expectBody(FoundLastRounds.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(4, res.getResponseBody().getRounds().size());
        });
  }

  @Test
  @DisplayName("round/add")
  public void addRoundTest() {
    when(roundRepository.saveRound(any())).thenReturn(Mono.just(100L));
    webClient.post()
        .uri("/round/add")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new AddRoundDto(1L, 2L, 3L, 4L, true)))
        .exchange()
        .expectStatus().isOk()
        .expectBody(IdDto.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(100L, res.getResponseBody().getId());
        });
  }
}
