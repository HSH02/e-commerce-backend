package com.ecommerce.api.v1.product.controller;


import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.global.utils.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 상품 관리", description = "관리자용 상품 관리 API")
public interface ProductAPiSpecification {


    @Operation(summary = "상품 검색", description = "상품을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 재고 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Product> searchProduct(
            @Valid @RequestBody UpdateStockRequest request
    );

    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Product> searchProductDetail(
            @Parameter(description = "상품 ID") @PathVariable Long productId
    );


}