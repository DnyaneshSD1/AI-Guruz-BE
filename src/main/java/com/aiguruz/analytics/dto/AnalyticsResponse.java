package com.aiguruz.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalDocuments;
    private long processedDocuments;
    private long pendingDocuments;
    private long totalTenants;
}
