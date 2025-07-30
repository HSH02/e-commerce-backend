package com.ecommerce.api.v1.admin.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
        String name,
        String description,

        @Positive(message = "상품 가격은 0보다 커야 합니다.")
        BigDecimal price,

        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        List<String> imageUrls,
        Boolean isActive
) {
}