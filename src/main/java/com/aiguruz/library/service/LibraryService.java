package com.aiguruz.library.service;

import com.aiguruz.common.exception.ResourceNotFoundException;
import com.aiguruz.library.model.LibraryDocument;
import com.aiguruz.library.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository repo;

    public LibraryDocument create(LibraryDocument doc) {
        return repo.save(doc);
    }

    public List<LibraryDocument> list(String tenantId) {
        return repo.findByTenantId(tenantId);
    }

    public LibraryDocument get(String id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Library document not found: " + id));
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}
