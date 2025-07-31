package com.ecommerce.api.v1.product.dto.request;

import com.ecommerce.domain.product.entity.ProductStatus;

import java.math.BigDecimal;

public record ProductSearchCondition(
        // 기본 검색
        String keyword,
        String category,
        String brand,

        // 가격 필터
        BigDecimal minPrice,
        BigDecimal maxPrice,

        // 재고/상태
        Boolean inStock,
        ProductStatus status

) {
    // 기본값을 가진 정적 팩토리 메서드
    public static ProductSearchCondition of(
            String keyword,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            ProductStatus status
    ) {
        return new ProductSearchCondition(
                keyword, category, brand,
                minPrice, maxPrice, inStock, status
        );
    }
}