package com.aiguruz.tenant.repository;

import com.aiguruz.tenant.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TenantRepository extends MongoRepository<Tenant, String> {
}
