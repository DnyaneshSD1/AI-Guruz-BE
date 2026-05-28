package com.aiguruz.document.dto;

import lombok.*;
import java.time.Instant;

@Data @Builder
public class DocumentDto {
    private String  id, originalFilename, status;
    private String  failureReason, mimeType, summaryId;
    private long    fileSizeBytes;
    private Instant uploadedAt, processedAt;
}

