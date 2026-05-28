package com.aiguruz.tenant.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tenants")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {
    @Id private String id;
    private String name;
    private String domain;
    private int    userCount;
    private String status;
    @CreatedDate private Instant createdAt;
}
