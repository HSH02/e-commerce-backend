package com.ecommerce.api.v1.product.controller;


import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductStatusRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.global.utils.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "상품 관리", description = "상품 관련 API")
public interface ProductAPiSpecification {

    @Operation(summary = "상품 등록", description = "새로운 상품을 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    RsData<ProductResponseDto> addProduct(
            @Valid @RequestBody AddProductRequest request
    );

    @Operation(summary = "상품 정보 수정", description = "기존 상품의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<ProductResponseDto> updateProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request
    );

    @Operation(summary = "상품 재고 관리", description = "상품의 재고 수량을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 재고 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<ProductResponseDto> manageProductStock(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request
    );

    @Operation(summary = "상품 상태 관리", description = "상품의 상태 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<ProductResponseDto> manageProductStock(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateProductStatusRequest request
    );

    @Operation(summary = "상품 삭제", description = "상품을 시스템에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> deleteProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId
    );

    @Operation(summary = "상품 검색", description = "상품을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 재고 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Product> searchProduct(
            // 기본 검색
            @RequestParam(required = false) String keyword,          // 상품명/설명 검색
            @RequestParam(required = false) String category,         // 카테고리
            @RequestParam(required = false) String brand,            // 브랜드

            // 가격 필터
            @RequestParam(required = false) BigDecimal minPrice,     // 최소 가격
            @RequestParam(required = false) BigDecimal maxPrice,     // 최대 가격

            // 재고/상태
            @RequestParam(required = false, defaultValue = "true") Boolean inStock,  // 재고 있는 상품만
            @RequestParam(required = false) String status,          // 상품 상태 (ACTIVE, INACTIVE, DISCONTINUED)

            // 평점/리뷰
            @RequestParam(required = false) Double minRating,       // 최소 평점
            @RequestParam(required = false) Integer minReviewCount, // 최소 리뷰 수

            // 정렬
            @RequestParam(defaultValue = "createdAt") String sortBy, // 정렬 기준
            @RequestParam(defaultValue = "desc") String sortDir,     // 정렬 방향

            // 페이징
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<ProductResponseDto> searchProductDetail(
            @Parameter(description = "상품 ID") @PathVariable Long productId
    );


}