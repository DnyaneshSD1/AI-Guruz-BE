package com.aiguruz.tenant.service;

import com.aiguruz.tenant.model.Tenant;
import com.aiguruz.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepo;

    public Tenant create(Tenant tenant) {
        return tenantRepo.save(tenant);
    }

    public List<Tenant> list() {
        return tenantRepo.findAll();
    }

    public Tenant get(String id) {
        return tenantRepo.findById(id).orElseThrow(() ->
            new IllegalArgumentException("Tenant not found: " + id));
    }

    public void delete(String id) {
        tenantRepo.deleteById(id);
    }
}
