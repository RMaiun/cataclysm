package com.mairo.cataclysm.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.dto.IdDto;
import com.mairo.cataclysm.repository.PlayerRepository;
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
class PlayerControllerTest {

  @Autowired
  private ApplicationContext ctx;

  @MockBean
  PlayerRepository repository;

  private static WebTestClient webClient;

  @BeforeEach
  void setup() {
    webClient = WebTestClient.bindToApplicationContext(ctx).build();
  }

  @Test
  @DisplayName("Context test case")
  void playersAllTest() {
    Player p = new Player();
    p.setId(1L);
    p.setSurname("test");
    when(repository.listAll()).thenReturn(Mono.just(List.of(p)));

    webClient.get()
        .uri("/players/all")
        .exchange()
        .expectStatus().isOk()
        .expectBody(FoundAllPlayers.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(1, res.getResponseBody().getPlayers().size());
          assertEquals(p.getSurname(), res.getResponseBody().getPlayers().get(0).getSurname());
        });
    verify(repository, times(1)).listAll();
  }

  @Test
  @DisplayName("Context test case")
  void playersAddTest() {
    Player p = new Player();
    p.setId(1L);
    p.setSurname("test");
    when(repository.findLastId()).thenReturn(Mono.just(34L));
    when(repository.savePlayer(any(Player.class))).thenReturn(Mono.just(35L));

    webClient.post()
        .uri("/players/add")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new AddPlayerDto("Testuser", "1444")))
        .exchange()
        .expectStatus().isOk()
        .expectBody(IdDto.class)
        .consumeWith(res -> {
          assertNotNull(res.getResponseBody());
          assertEquals(35, res.getResponseBody().getId());
        });
  }
}
