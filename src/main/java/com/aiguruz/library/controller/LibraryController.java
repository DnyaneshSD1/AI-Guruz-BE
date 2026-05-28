package com.aiguruz.library.controller;

import com.aiguruz.common.model.ApiResponse;
import com.aiguruz.common.util.SecurityUtils;
import com.aiguruz.library.model.LibraryDocument;
import com.aiguruz.library.service.LibraryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Tag(name = "Library", description = "Shared document library")
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping
    public ResponseEntity<ApiResponse<LibraryDocument>> create(@RequestBody LibraryDocument body) {
        body.setTenantId(SecurityUtils.currentTenantId());
        body.setUploadedBy(SecurityUtils.currentUserId());
        return ResponseEntity.status(201).body(ApiResponse.ok(libraryService.create(body), "Library document added"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibraryDocument>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(libraryService.list(SecurityUtils.currentTenantId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryDocument>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(libraryService.get(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        libraryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Library document removed"));
    }
}
