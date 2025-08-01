package com.ecommerce.api.v1.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "장바구니 상품 정보 DTO")
public class CartItemDto {
    
    @Schema(description = "상품 ID", example = "1")
    private Long productId;
    
    @Schema(description = "상품 이름", example = "청자켓")
    private String productName;
    
    @Schema(description = "상품 가격", example = "89000")
    private BigDecimal price;
    
    @Schema(description = "수량", example = "2")
    private Integer quantity;
    
    @Schema(description = "상품 이미지 URL", example = "http://example.com/image.jpg")
    private String imageUrl;
    
    @Schema(description = "상품 소계 (가격 * 수량)", example = "178000")
    private BigDecimal subtotal;
}