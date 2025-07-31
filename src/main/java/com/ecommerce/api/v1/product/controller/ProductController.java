package com.ecommerce.api.v1.product.controller;

import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductStatusRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.global.utils.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController implements ProductAPiSpecification {

    private final ProductService productService;

    @PostMapping("")
    public RsData<ProductResponseDto> addProduct(
            @RequestBody @Valid AddProductRequest request
    ) {
        ProductResponseDto product = productService.addProduct(request);
        return RsData.success(HttpStatus.CREATED, product, "상품 등록이 완료되었습니다");
    }

    @PutMapping("/{productId}")
    public RsData<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        ProductResponseDto product = productService.updateProduct(productId, request);
        return RsData.success(HttpStatus.OK, product, "상품 수정이 완료되었습니다");
    }

    @PutMapping("/{productId}/stock")
    public RsData<ProductResponseDto> manageProductStock(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequest request
    ) {
        ProductResponseDto product = productService.manageProductStock(productId, request);
        return RsData.success(HttpStatus.OK, product, "재고 수정이 완료되었습니다");
    }

    @PutMapping("/{productId}/status")
    public RsData<ProductResponseDto> manageProductStock(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductStatusRequest request
    ) {
        ProductResponseDto product = productService.manageProductStatus(productId, request);
        return RsData.success(HttpStatus.OK, product, "상태 수정이 완료되었습니다");
    }

    @DeleteMapping("/{productId}")
    public RsData<Void> deleteProduct(
            @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return RsData.success(HttpStatus.OK, null, "상품 삭제가 완료되었습니다");
    }

    @GetMapping("/search")
    public RsData<Product> searchProduct(
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
            @RequestParam(defaultValue = "0") int page,             // 페이지
            @RequestParam(defaultValue = "20") int size             // 사이즈
    ) {
        return null;
    }

    @GetMapping("/{productId}")
    public RsData<ProductResponseDto> searchProductDetail(
            @PathVariable Long productId
    ) {
        ProductResponseDto product = productService.findProductResponseById(productId);
        return RsData.success(HttpStatus.OK, product, "상품 조회가 완료되었습니다");
    }
}
