package com.ecommerce.api.v1.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AddProductRequest(
        @NotBlank(message = "상품 이름은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "상품 가격은 필수입니다.")
        @Positive(message = "상품 가격은 0보다 커야 합니다.")
        BigDecimal price,

        @NotNull(message = "재고 수량은 필수입니다.")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity

) {

}
