package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.GenerateStatsDocumentDto;
import com.mairo.cataclysm.dto.ImportDumpDto;
import com.mairo.cataclysm.service.ExportService;
import com.mairo.cataclysm.service.ImportService;
import com.mairo.cataclysm.service.ReportGeneratorService;
import com.mairo.cataclysm.utils.DateUtils;
import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
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
public class BinaryController implements BinaryResponseSupport{

  private final ReportGeneratorService reportGeneratorService;
  private final ExportService exportService;
  private final ImportService importService;

  @GetMapping("/reports/xlsx/{season}")
  public Mono<ResponseEntity<InputStreamResource>> xlsxReport(@PathVariable String season) {
    return binaryResponse(reportGeneratorService.generateXslxReport(new GenerateStatsDocumentDto(season)));
  }

  @PostMapping(value = "/dump/import/{moderator}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ImportDumpDto> importDump(@RequestPart("file") Mono<FilePart> filePartMono, @PathVariable String moderator) {
    return filePartMono.flatMap(filePart -> importService.importDump(filePart, moderator));
  }

  @GetMapping(value = "/dump/export/{moderator}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<ResponseEntity<InputStreamResource>> exportDump(@PathVariable String moderator) {
    ZonedDateTime now = DateUtils.now();
    return binaryResponse(exportService.export(now, moderator));
  }
}
