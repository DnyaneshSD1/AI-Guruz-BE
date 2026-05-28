package com.aiguruz.audit.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_logs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {
    @Id private String id;
    private String userId;
    private String email;
    private String tenantId;
    private String action;
    private String status;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> metadata;
    @CreatedDate private Instant timestamp;
}
