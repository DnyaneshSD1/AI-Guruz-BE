package com.aiguruz.ai.repository;

import com.aiguruz.ai.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface AiSessionRepository extends MongoRepository<AiSession, String> {
    List<AiSession> findByUserIdOrderByUpdatedAtDesc(String userId);
}


