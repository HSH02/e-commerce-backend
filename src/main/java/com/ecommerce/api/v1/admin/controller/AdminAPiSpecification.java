package com.ecommerce.api.v1.admin.controller;


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
public interface AdminAPiSpecification {

    @Operation(summary = "상품 등록", description = "새로운 상품을 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/products")
    RsData<Product> addProduct(@Valid @RequestBody AddProductRequest request);

    @Operation(summary = "상품 정보 수정", description = "기존 상품의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @PutMapping("/products/{productId}")
    RsData<Product> updateProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request);

    @Operation(summary = "상품 재고 관리", description = "상품의 재고 수량을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 재고 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @PutMapping("/products/{productId}/stock")
    RsData<Product> manageProductStock(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request);

    @Operation(summary = "상품 삭제", description = "상품을 시스템에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @DeleteMapping("/products/{productId}")
    RsData<Void> deleteProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId);
}