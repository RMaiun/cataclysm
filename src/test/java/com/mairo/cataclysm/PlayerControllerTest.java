package com.mairo.cataclysm;


import com.mairo.cataclysm.controller.PlayerController;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.dto.FoundAllPlayers;
import com.mairo.cataclysm.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayerControllerTest {

  @Autowired
  private ApplicationContext ctx;

  @MockBean
  PlayerRepository repository;

  private static WebTestClient webClient;

  @BeforeEach
  public void setup(){
    webClient = WebTestClient.bindToApplicationContext(ctx).build();
  }

  @Test
  @DisplayName("Context test case")
  public void test() {
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

}
