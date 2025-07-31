package com.ecommerce.api.v1.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 등록 요청 DTO")
public record AddProductRequest(
        @Schema(description = "상품 이름", example = "새로운 청자켓")
        @NotBlank(message = "상품 이름은 필수입니다.")
        String name,

        @Schema(description = "상품 설명", example = "2025년 신상 청자켓입니다.")
        String description,

        @Schema(description = "상품 브랜드", example = "ABC")
        String brand,

        @Schema(description = "상품 가격", example = "89000")
        @NotNull(message = "상품 가격은 필수입니다.")
        @Positive(message = "상품 가격은 0보다 커야 합니다.")
        BigDecimal price,

        @Schema(description = "재고 수량", example = "100")
        @NotNull(message = "재고 수량은 필수입니다.")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        @Schema(description = "상품 이미지 URL 목록", example = "[\"http://example.com/image.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "카테고리 이름 목록", example = "[\"의류\", \"아우터\"]")
        @NotEmpty(message = "카테고리는 최소 1개 이상 등록해야 합니다.")
        List<@NotBlank(message = "카테고리 이름은 공백일 수 없습니다.") String> categoryNames
) {
}