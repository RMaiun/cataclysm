package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.domain.AuditLog;
import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.GenerateLogsFileDto;
import com.mairo.cataclysm.dto.StoreAuditLogDto;
import com.mairo.cataclysm.repository.AuditLogRepository;
import com.mairo.cataclysm.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("log")
@RequiredArgsConstructor
/*
 * Tmp solution, rewrite ASAP
 */
public class AuditLogController implements BinaryResponseSupport {

  private static final Logger log = LogManager.getLogger(AuditLogController.class);

  private final AuditLogRepository auditLogRepository;

  @PostMapping("/store")
  public Mono<AuditLog> storeAuditLog(@RequestBody StoreAuditLogDto storeAuditLogDto) {
    return Mono.just(storeAuditLogDto)
        .doOnNext((dto) -> log.info(dto.getMsg()))
        .then(auditLogRepository.save(new AuditLog(null, storeAuditLogDto.getMsg(), DateUtils.now())));
  }

  @PostMapping(value = "/logReport/{moderator}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<ResponseEntity<InputStreamResource>> generateLogsReport(@PathVariable String moderator, @RequestBody GenerateLogsFileDto dto) {

    Criteria criteria = new Criteria().andOperator(Criteria.where("created").gte(dto.getFrom()), Criteria.where("created").lte(dto.getTo()));
    return binaryResponse(auditLogRepository.listByCriteria(criteria)
        .publishOn(Schedulers.elastic())
        .reduce(new StringBuilder(), (sb, log) -> sb.append(log).append(System.lineSeparator()))
        .map(sb -> new BinaryFileDto(sb.toString().getBytes(), "auditLog", "log"))
    );
  }
}
