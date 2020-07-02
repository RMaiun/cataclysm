package com.mairo.cataclysm;

import com.mairo.cataclysm.config.AppProperties;
import com.mairo.cataclysm.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Context test case")
class CataclysmApplicationTests {

  @Autowired
  private PlayerRepository playerRepository;
  @Autowired
  private AppProperties appProperties;

  @Test
  @DisplayName("Load context test")
  void contextLoads() {
    assertNotNull(playerRepository);
    assertNotNull(appProperties);
    assertEquals(25, appProperties.getWinPoints());
  }

}
