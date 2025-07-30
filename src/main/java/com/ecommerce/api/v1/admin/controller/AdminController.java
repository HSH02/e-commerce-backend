package com.ecommerce.api.v1.admin.controller;

import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.global.utils.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// TODO 관리자 권한
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController implements AdminAPiSpecification {

    private final ProductService productService;

    @PostMapping("/products")
    public RsData<Product> addProduct(
            @RequestBody @Valid AddProductRequest request
    ) {
        Product product = productService.addProduct(request);
        return RsData.success(HttpStatus.CREATED, product, "상품 등록이 완료되었습니다");
    }

    @PutMapping("/products/{productId}")
    public RsData<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        Product product = productService.updateProduct(productId, request);
        return RsData.success(HttpStatus.OK, product, "상품 수정이 완료되었습니다");
    }

    @PutMapping("/products/{productId}/stock")
    public RsData<Product> manageProductStock(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequest request
    ) {
        Product product = productService.manageProductStock(productId, request);
        return RsData.success(HttpStatus.OK, product, "재고 수정이 완료되었습니다");
    }

    @DeleteMapping("/products/{productId}")
    public RsData<Void> deleteProduct(
            @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return RsData.success(HttpStatus.OK, null, "상품 삭제가 완료되었습니다");
    }
}
