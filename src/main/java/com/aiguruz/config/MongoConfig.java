package com.aiguruz.config;

import com.aiguruz.audit.model.AuditLog;
import com.aiguruz.document.model.Document;
import com.aiguruz.user.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoConfig {

    private final MongoTemplate mongo;

    @PostConstruct
    public void ensureIndexes() {
        log.info("Ensuring MongoDB indexes...");

        mongo.indexOps(User.class)
            .ensureIndex(new Index("email", Sort.Direction.ASC).unique());

        mongo.indexOps(Document.class)
            .ensureIndex(new Index("uploadedBy", Sort.Direction.ASC));
        mongo.indexOps(Document.class)
            .ensureIndex(new Index("tenantId", Sort.Direction.ASC));
        mongo.indexOps(Document.class)
            .ensureIndex(new Index("textractJobId", Sort.Direction.ASC).sparse());

        mongo.indexOps(AuditLog.class)
            .ensureIndex(new CompoundIndexDefinition(
                new org.bson.Document("tenantId", 1).append("timestamp", -1)));

        log.info("MongoDB indexes ensured.");
    }
}

