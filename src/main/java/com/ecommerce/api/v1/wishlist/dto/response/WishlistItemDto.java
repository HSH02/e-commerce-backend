package com.ecommerce.api.v1.wishlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "위시리스트 상품 정보 DTO")
public class WishlistItemDto {
    
    @Schema(description = "상품 ID", example = "1")
    private Long productId;
    
    @Schema(description = "상품 이름", example = "청자켓")
    private String productName;
    
    @Schema(description = "상품 가격", example = "89000")
    private BigDecimal price;
    
    @Schema(description = "상품 이미지 URL", example = "http://example.com/image.jpg")
    private String imageUrl;
    
    @Schema(description = "위시리스트에 추가된 시간")
    private LocalDateTime addedAt;
}