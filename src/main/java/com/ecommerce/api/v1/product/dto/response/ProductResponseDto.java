package com.ecommerce.api.v1.product.dto.response;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String brand,
        ProductStatus status,
        String statusDescription,
        List<String> imageUrls,
        Set<CategoryDto> categories,
        boolean availableForSale
) {
    public static ProductResponseDto from(Product product) {
        List<String> imageUrls = product.getImages().stream()
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());

        Set<CategoryDto> categoryDtos = product.getCategories().stream()
                .map(CategoryDto::from)
                .collect(Collectors.toSet());

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getBrand(),
                product.getStatus(),
                product.getStatus().getDescription(),
                imageUrls,
                categoryDtos,
                product.isAvailableForSale()
        );
    }
}