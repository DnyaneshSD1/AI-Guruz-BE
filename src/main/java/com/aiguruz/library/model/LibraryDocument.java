package com.aiguruz.library.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "library_documents")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LibraryDocument {
    @Id private String id;
    private String tenantId;
    private String title;
    private String author;
    private String s3Key;
    private int    pageCount;
    private String status;
    private String uploadedBy;
    @CreatedDate private Instant createdAt;
}
