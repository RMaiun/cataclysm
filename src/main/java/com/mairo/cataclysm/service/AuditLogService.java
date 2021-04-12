package com.mairo.cataclysm.service;

import static com.mairo.cataclysm.validation.Validator.validate;

import com.mairo.cataclysm.domain.AuditLog;
import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.DistributeLogsReportDto;
import com.mairo.cataclysm.dto.StoreAuditLogDto;
import com.mairo.cataclysm.repository.AuditLogRepository;
import com.mairo.cataclysm.utils.DateUtils;
import com.mairo.cataclysm.validation.ValidationTypes;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private static final Logger log = LogManager.getLogger(AuditLogService.class);

  private final AuditLogRepository auditLogRepository;

  public Mono<AuditLog> storeAuditLog(StoreAuditLogDto dto) {
    return validate(dto, ValidationTypes.storeAuditLogValidationType)
        .then(auditLogRepository.save(new AuditLog(null, dto.getMsg(), DateUtils.now())))
        .doOnNext(al -> log.info(al.getMsg()));
  }

  public Mono<BinaryFileDto> distributeLogsReport(DistributeLogsReportDto dto) {
    Criteria criteria = new Criteria().andOperator(Criteria.where("created").gte(dto.getFrom()), Criteria.where("created").lte(dto.getTo()));
    return auditLogRepository.listByCriteria(criteria)
        .publishOn(Schedulers.elastic())
        .reduce(new StringBuilder(), (sb, log) -> sb.append(log.getMsg()).append(System.lineSeparator()))
        .map(sb -> new BinaryFileDto(sb.toString().getBytes(), "auditLog", "log"));
  }

}
