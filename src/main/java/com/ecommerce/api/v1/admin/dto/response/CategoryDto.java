package com.ecommerce.api.v1.admin.dto.response;

import com.ecommerce.domain.product.entity.Category;

public record CategoryDto(
        Long id,
        String name
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
