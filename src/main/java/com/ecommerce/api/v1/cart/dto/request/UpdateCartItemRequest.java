package com.ecommerce.api.v1.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "장바구니 상품 수량 수정 요청 DTO")
public record UpdateCartItemRequest(
        @Schema(description = "수량", example = "2")
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}