package com.ecommerce.api.v1.wishlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "위시리스트 응답 DTO")
public class WishlistResponseDto {
    
    @Schema(description = "위시리스트에 담긴 상품 목록")
    private List<WishlistItemDto> items;
    
    @Schema(description = "위시리스트에 담긴 상품 개수")
    private int totalItems;
}