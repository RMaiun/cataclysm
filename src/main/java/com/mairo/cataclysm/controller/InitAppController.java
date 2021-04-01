package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.helper.MigrationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InitAppController {

  private final MigrationHelper migrationHelper;

  @GetMapping("app/init")
  public void initApp(){
    migrationHelper.migrate();
  }

}
