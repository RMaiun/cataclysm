package com.mairo.cataclysm.core.service;

import static com.mairo.cataclysm.core.validation.Validator.validate;

import com.mairo.cataclysm.core.domain.AuditLog;
import com.mairo.cataclysm.core.dto.BinaryFileDto;
import com.mairo.cataclysm.core.dto.DistributeLogsReportDto;
import com.mairo.cataclysm.core.dto.StoreAuditLogDto;
import com.mairo.cataclysm.core.repository.AuditLogRepository;
import com.mairo.cataclysm.core.utils.DateUtils;
import com.mairo.cataclysm.core.validation.ValidationTypes;
import com.mairo.cataclysm.core.validation.Validator;
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
    return Validator.validate(dto, ValidationTypes.storeAuditLogValidationType)
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
