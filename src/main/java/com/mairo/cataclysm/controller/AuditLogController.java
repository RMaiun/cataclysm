package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.dto.DistributeLogsReportDto;
import com.mairo.cataclysm.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("log")
@RequiredArgsConstructor
/*
 * Tmp solution, rewrite ASAP
 */
public class AuditLogController implements BinaryResponseSupport {

  private static final Logger log = LogManager.getLogger(AuditLogController.class);

  private final AuditLogService auditLogService;

  @PostMapping(value = "/report", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<ResponseEntity<InputStreamResource>> generateLogsReport(@RequestBody DistributeLogsReportDto dto) {
    return binaryResponse(auditLogService.distributeLogsReport(dto));
  }
}
