package com.aiguruz.ai.repository;

import com.aiguruz.ai.model.Summary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SummaryRepository extends MongoRepository<Summary, String> {
    Optional<Summary> findByDocumentId(String documentId);
}
