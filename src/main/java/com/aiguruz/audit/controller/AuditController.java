package com.aiguruz.audit.controller;

import com.aiguruz.audit.model.AuditLog;
import com.aiguruz.audit.service.AuditService;
import com.aiguruz.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log query")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(auditService.findAll()));
    }
}
