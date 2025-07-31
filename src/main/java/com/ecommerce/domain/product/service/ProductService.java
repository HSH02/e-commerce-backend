package com.ecommerce.domain.product.service;

import com.ecommerce.api.v1.product.dto.request.*;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.Category;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.repository.ProductRepository;
import com.ecommerce.global.utils.dto.SliceResponseDto;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;

    @Transactional
    public ProductResponseDto addProduct(AddProductRequest request) {
        Set<Category> categories = categoryService.findOrCreateCategories(request.categoryNames());

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .brand(request.brand())
                .stockQuantity(request.stockQuantity())
                .status(ProductStatus.ACTIVE)
                .category(categories)
                .build();

        Product savedProduct = productRepository.save(product);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            productImageService.createProductImages(savedProduct, request.imageUrls());
        }

        return ProductResponseDto.from(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productId, UpdateProductRequest request) {
        Product product = findProductEntityById(productId);

        // 카테고리 업데이트
        if (request.categoryNames() != null && !request.categoryNames().isEmpty()) {
            Set<Category> categories = categoryService.findOrCreateCategories(request.categoryNames());
            product.updateCategories(categories);
        }

        product.updateDetails(
                request.name(),
                request.description(),
                request.brand(),
                request.price(),
                request.stockQuantity(),
                request.status()
        );

        // 이미지 업데이트
        if (request.imageUrls() != null) {
            productImageService.updateProductImages(product, request.imageUrls());
        }

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto manageProductStock(Long productId, UpdateStockRequest request) {
        Product product = findProductEntityById(productId);
        product.updateStock(request.stockQuantity());
        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto manageProductStatus(Long productId,  UpdateProductStatusRequest request) {
        Product product = findProductEntityById(productId);
        product.updateStatus(request.status());
        return ProductResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductEntityById(productId);
        product.setIsDeleted(true);
    }

    @Transactional
    public ProductResponseDto updateProductStatus(Long productId, ProductStatus status) {
        Product product = findProductEntityById(productId);
        product.updateStatus(status);
        return ProductResponseDto.from(product);
    }

    public ProductResponseDto findProductResponseById(Long productId) {
        return ProductResponseDto.from(findProductEntityById(productId));
    }

    public Product findProductEntityById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SliceResponseDto<ProductResponseDto> searchProductsForInfiniteScroll(
            ProductSearchCondition condition,
            ProductSearchPageRequest pageRequest
    ) {
        Pageable pageable = pageRequest.toPageable();

        Slice<Product> products = productRepository.findProductsWithConditions(
                condition.keyword(),
                condition.category(),
                condition.brand(),
                condition.minPrice(),
                condition.maxPrice(),
                condition.inStock(),
                condition.status(),
                pageable
        );

        Slice<ProductResponseDto> productSlice = products.map(ProductResponseDto::from);
        return SliceResponseDto.from(productSlice);
    }
}