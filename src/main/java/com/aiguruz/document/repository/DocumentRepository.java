package com.aiguruz.document.repository;

import com.aiguruz.document.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface DocumentRepository extends MongoRepository<Document, String> {
    List<Document>     findByUploadedBy(String userId);
    Optional<Document> findByTextractJobId(String jobId);
    List<Document>     findByTenantId(String tenantId);
    long               countByTenantId(String tenantId);
}

