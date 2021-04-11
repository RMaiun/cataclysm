package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuditLogRepository {

  private final ReactiveMongoTemplate template;

  public Mono<AuditLog> save(AuditLog auditLog){
    return template.save(auditLog);
  }

  public Flux<AuditLog> listByCriteria(Criteria criteria){
    return template.find(new Query(criteria),AuditLog.class);
  }

}
