package com.ecommerce.api.v1.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "장바구니 응답 DTO")
public class CartResponseDto {
    
    @Schema(description = "장바구니에 담긴 상품 목록")
    private List<CartItemDto> items;
    
    @Schema(description = "장바구니에 담긴 상품 개수")
    private int totalItems;
    
    @Schema(description = "장바구니 총 금액", example = "178000")
    private BigDecimal totalAmount;
}