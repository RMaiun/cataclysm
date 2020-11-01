package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.GenerateStatsDocumentDto;
import com.mairo.cataclysm.dto.ImportDumpDto;
import com.mairo.cataclysm.service.ExportService;
import com.mairo.cataclysm.service.ImportService;
import com.mairo.cataclysm.service.ReportGeneratorService;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class BinaryController {

  private final ReportGeneratorService reportGeneratorService;
  private final ExportService exportService;
  private final ImportService importService;

  @GetMapping("/reports/xlsx/{season}")
  public Mono<ResponseEntity<InputStreamResource>> xlsxReport(@PathVariable String season) {
    return reportGeneratorService.generateXslxReport(new GenerateStatsDocumentDto(season))
        .map(res -> {
          String fileName = String.format("%s.%s", res.getFileName().replace("|", "_"), res.getExtension());
          return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName))
              .body(new InputStreamResource(new ByteArrayInputStream(res.getData())));
        });
  }

  @PostMapping(value = "/dump/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ImportDumpDto> exportDump(@RequestPart("file") Mono<FilePart> filePartMono) {
    return filePartMono.flatMap(importService::importDump);
  }
}
