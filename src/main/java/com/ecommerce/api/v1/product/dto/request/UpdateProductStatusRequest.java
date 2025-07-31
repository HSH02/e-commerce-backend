package com.ecommerce.api.v1.product.dto.request;

import com.ecommerce.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "상품 상태는 필수입니다")
        ProductStatus status
) {
}