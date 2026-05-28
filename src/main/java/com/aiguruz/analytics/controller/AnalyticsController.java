package com.aiguruz.analytics.controller;

import com.aiguruz.analytics.dto.AnalyticsResponse;
import com.aiguruz.analytics.service.AnalyticsService;
import com.aiguruz.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Platform analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','SUPERADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.overview()));
    }
}
