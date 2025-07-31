package com.ecommerce.api.v1.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "상품 수정 요청 DTO")
public record UpdateProductRequest(
        @Schema(description = "새 상품 이름", example = "빈티지 데님 자켓")
        String name,

        @Schema(description = "새 상품 설명", example = "워싱이 매력적인 빈티지 데님 자켓")
        String description,

        @Schema(description = "새 상품 가격", example = "95000")
        @Positive(message = "상품 가격은 0보다 커야 합니다.")
        BigDecimal price,

        @Schema(description = "새 재고 수량", example = "50")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        @Schema(description = "상품 이미지 URL 목록", example = "[\"http://example.com/image.jpg\"]")
        List<String> imageUrls,

        @Schema(description = "상품 활성화 여부", example = "true")
        Boolean isActive,

        @Schema(description = "새 카테고리 이름 목록. 비어있으면 카테고리는 수정되지 않음.", example = "[\"의류\", \"시즌오프\"]")
        List<@NotBlank(message = "카테고리 이름은 공백일 수 없습니다.") String> categoryNames
) {
}