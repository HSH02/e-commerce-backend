package com.ecommerce.api.v1.product.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record ProductSearchPageRequest(
    int page,
    int size, 
    String sortBy,
    String sortDir
) {
    public static ProductSearchPageRequest of(int page, int size, String sortBy, String sortDir) {
        return new ProductSearchPageRequest(page, size, sortBy, sortDir);
    }
    
    public Pageable toPageable() {
        Sort sort = sortDir.equals("desc") ?
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}