package com.aiguruz.audit.service;

import com.aiguruz.audit.model.AuditLog;
import com.aiguruz.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository repo;

    @Async
    public void log(String userId, String tenantId, String email,
                    String action, String status, String ipAddress,
                    Map<String, Object> metadata) {
        var audit = AuditLog.builder()
            .userId(userId)
            .email(email)
            .tenantId(tenantId)
            .action(action)
            .status(status)
            .ipAddress(ipAddress)
            .userAgent(null)
            .metadata(metadata)
            .build();

        repo.save(audit);
        log.debug("Audit event written: {} userId={} tenantId={}", action, userId, tenantId);
    }

    public List<AuditLog> findAll() {
        return repo.findAll();
    }
}
