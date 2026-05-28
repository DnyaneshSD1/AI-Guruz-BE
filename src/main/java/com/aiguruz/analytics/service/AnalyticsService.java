package com.aiguruz.analytics.service;

import com.aiguruz.analytics.dto.AnalyticsResponse;
import com.aiguruz.document.model.Document;
import com.aiguruz.document.repository.DocumentRepository;
import com.aiguruz.tenant.repository.TenantRepository;
import com.aiguruz.user.model.User;
import com.aiguruz.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository     userRepo;
    private final DocumentRepository documentRepo;
    private final TenantRepository   tenantRepo;

    public AnalyticsResponse overview() {
        long totalUsers     = userRepo.count();
        long activeUsers    = userRepo.findAll().stream().filter(User::isActive).count();
        long totalDocuments = documentRepo.count();

        List<Document> allDocs = documentRepo.findAll();
        long processedDocs = allDocs.stream().filter(d -> "READY".equals(d.getStatus()) || "DONE".equals(d.getStatus()))
            .count();
        long pendingDocs = allDocs.stream()
            .filter(d -> "UPLOADED".equals(d.getStatus()) || "EXTRACTING".equals(d.getStatus()) || "SUMMARIZING".equals(d.getStatus()))
            .count();
        long totalTenants = tenantRepo.count();

        return AnalyticsResponse.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .totalDocuments(totalDocuments)
            .processedDocuments(processedDocs)
            .pendingDocuments(pendingDocs)
            .totalTenants(totalTenants)
            .build();
    }
}
