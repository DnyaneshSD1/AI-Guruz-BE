package com.aiguruz.audit.repository;

import com.aiguruz.audit.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditRepository extends MongoRepository<AuditLog, String> {
}
