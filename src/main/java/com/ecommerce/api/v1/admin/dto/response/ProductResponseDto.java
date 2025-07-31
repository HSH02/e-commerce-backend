package com.ecommerce.api.v1.admin.dto.response;

import com.ecommerce.domain.product.entity.Product;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public record ProductResponseDto(
    Long id,
    String name,
    BigDecimal price,
    Integer stockQuantity,
    Set<CategoryDto> categories
) {
    public static ProductResponseDto from(Product product) {
        Set<CategoryDto> categoryDtos = product.getCategories().stream()
                .map(CategoryDto::from)
                .collect(Collectors.toSet());

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                categoryDtos
        );
    }
}

