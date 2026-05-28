package com.aiguruz.document.controller;

import com.aiguruz.ai.dto.SummaryResponse;
import com.aiguruz.ai.service.SummaryService;
import com.aiguruz.common.model.ApiResponse;
import com.aiguruz.common.util.SecurityUtils;
import com.aiguruz.document.dto.*;
import com.aiguruz.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "File upload, extraction, summary")
public class DocumentController {

    private final DocumentService docService;
    private final SummaryService  summaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF/DOCX/TXT — triggers Textract extraction")
    public ResponseEntity<ApiResponse<UploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) throws IOException {
        return ResponseEntity.status(201).body(ApiResponse.ok(
            docService.upload(file,
                SecurityUtils.currentUserId(),
                SecurityUtils.currentTenantId(),
                clientIp(req)),
            "Upload successful. Extraction in progress."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
            docService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentDto>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(docService.getById(id)));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Get a temporary pre-signed S3 download URL")
    public ResponseEntity<ApiResponse<String>> download(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(docService.presignedUrl(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        docService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Document deleted"));
    }

    @PostMapping("/{id}/summarize")
    @Operation(summary = "Generate AI summary (call after status=READY)")
    public ResponseEntity<ApiResponse<SummaryResponse>> summarize(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
            summaryService.generate(id, SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(summaryService.getByDocId(id)));
    }

    private String clientIp(HttpServletRequest r) {
        String xff = r.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : r.getRemoteAddr();
    }
}

