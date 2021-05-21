package com.mairo.cataclysm.core.repository;

import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.mairo.cataclysm.core.domain.AuditLog;
@Service
@RequiredArgsConstructor
public class AuditLogRepository {

  private final ReactiveMongoTemplate template;

  public Mono<AuditLog> save(AuditLog auditLog) {
    return template.save(auditLog);
  }

  public Flux<AuditLog> listByCriteria(Criteria criteria) {
    return template.find(new Query(criteria), AuditLog.class);
  }

  public Flux<AuditLog> listAll() {
    return template.findAll(AuditLog.class);
  }

  public Mono<Long> removeAll() {
    return template.remove(AuditLog.class)
        .all()
        .map(DeleteResult::getDeletedCount);
  }
}
