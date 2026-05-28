package com.aiguruz.common.model;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;
    private int     page, size, totalPages;
    private long    totalElements;

    public static <T> PageResponse<T> of(Page<T> page) {
        var r = new PageResponse<T>();
        r.content       = page.getContent();
        r.page          = page.getNumber();
        r.size          = page.getSize();
        r.totalPages    = page.getTotalPages();
        r.totalElements = page.getTotalElements();
        return r;
    }
}

