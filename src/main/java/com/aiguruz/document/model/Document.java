package com.aiguruz.document.model;

import lombok.*;
import org.springframework.data.annotation.*;

import java.time.Instant;

@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Document {
    @Id private String id;

    private String uploadedBy;      // userId
    private String tenantId;
    private String originalFilename;
    private String s3Key;           // tenants/{tid}/users/{uid}/{uuid}.pdf
    private String s3Bucket;
    private String mimeType;
    private long   fileSizeBytes;

    // UPLOADED → EXTRACTING → READY → SUMMARIZING → DONE | FAILED
    private String status;
    private String failureReason;
    private String textractJobId;   // async job reference
    private String extractedText;   // from Textract (stored; not returned in list DTOs)
    private String summaryId;

    @CreatedDate      private Instant uploadedAt;
    @LastModifiedDate private Instant updatedAt;
    private Instant processedAt;
}

