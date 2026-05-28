package com.aiguruz.tenant.controller;

import com.aiguruz.common.model.ApiResponse;
import com.aiguruz.tenant.model.Tenant;
import com.aiguruz.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Tenant administration")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Tenant>> create(@RequestBody Tenant tenant) {
        return ResponseEntity.status(201).body(ApiResponse.ok(tenantService.create(tenant), "Tenant created"));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<Tenant>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.list()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Tenant>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.get(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        tenantService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Tenant removed"));
    }
}
