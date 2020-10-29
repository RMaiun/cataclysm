package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.GenerateStatsDocumentDto;
import com.mairo.cataclysm.service.ReportGeneratorService;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportGeneratorService reportGeneratorService;

  @GetMapping("/xlsx/{season}")
  public Mono<ResponseEntity<InputStreamResource>> getFile(@PathVariable String season) {
    return reportGeneratorService.generateXslxReport(new GenerateStatsDocumentDto(season))
        .map(res -> {
          String fileName = String.format("%s.%s", res.getFileName().replace("|", "_"), res.getExtension());
          return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
              .body(new InputStreamResource(new ByteArrayInputStream(res.getData())));
        });
  }
}
