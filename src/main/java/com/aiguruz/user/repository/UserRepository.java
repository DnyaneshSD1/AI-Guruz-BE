package com.aiguruz.user.repository;

import com.aiguruz.user.model.User;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean        existsByEmail(String email);
    Page<User>     findByTenantId(String tenantId, Pageable pageable);
    long           countByTenantIdAndActive(String tenantId, boolean active);
}

