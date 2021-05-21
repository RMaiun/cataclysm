package com.mairo.cataclysm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mairo.cataclysm.properties.AppProps;
import com.mairo.cataclysm.properties.RabbitProps;
import com.mairo.cataclysm.core.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Context test case")
class CataclysmApplicationTests {

  @Autowired
  private PlayerRepository playerRepository;
  @Autowired
  private AppProps appProps;
  @Autowired
  private RabbitProps rabbitProps;

  @Test
  @DisplayName("Load context test")
  void contextLoads() {
    assertNotNull(playerRepository);
    assertNotNull(appProps);
    assertEquals(25, appProps.getWinPoints());
  }

  @Test
  @DisplayName("Check rabbit properties were load successfully")
  void rabbitPropertiesTest() {
    System.out.println(rabbitProps);
  }
}
