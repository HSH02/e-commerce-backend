package com.ecommerce.api.v1.product.dto.request;

import com.ecommerce.domain.product.entity.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 수정 요청 DTO")
public record UpdateProductRequest(
        @Schema(description = "상품 이름", example = "수정된 청자켓")
        @NotBlank(message = "상품명은 필수입니다")
        String name,
        
        @Schema(description = "상품 설명", example = "2025년 수정된 상품 설명입니다.")
        String description,
        
        @Schema(description = "상품 브랜드", example = "UPDATED_BRAND")
        @NotBlank(message = "브랜드는 필수입니다")
        String brand,
        
        @Schema(description = "상품 가격", example = "95000")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
        BigDecimal price,
        
        @Schema(description = "재고 수량", example = "80")
        @PositiveOrZero(message = "재고는 0 이상이어야 합니다")
        Integer stockQuantity,
        
        @Schema(description = "상품 이미지 URL 목록", example = "[\"http://example.com/updated-image1.jpg\", \"http://example.com/updated-image2.jpg\"]")
        List<String> imageUrls,
        
        @Schema(description = "상품 상태", example = "ACTIVE", 
                allowableValues = {"ACTIVE", "INACTIVE", "OUT_OF_STOCK", "DISCONTINUED", "PENDING"})
        ProductStatus status,
        
        @Schema(description = "카테고리 이름 목록", example = "[\"의류\", \"아우터\", \"캐주얼\"]")
        List<String> categoryNames
) {
}