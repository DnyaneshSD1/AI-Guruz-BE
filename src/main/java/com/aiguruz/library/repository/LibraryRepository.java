package com.aiguruz.library.repository;

import com.aiguruz.library.model.LibraryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LibraryRepository extends MongoRepository<LibraryDocument, String> {
    List<LibraryDocument> findByTenantId(String tenantId);
}
