package com.ecommerce.domain.product.service;

import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.admin.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.Category;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.repository.ProductRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public ProductResponseDto addProduct(AddProductRequest request) {
        Set<Category> categories = categoryService.findOrCreateCategories(request.categoryNames());

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .stockQuantity(request.stockQuantity())
                .category(categories)
                .build();

        productRepository.save(product);

        return ProductResponseDto.from(product);
    }


    @Transactional
    public ProductResponseDto updateProduct(Long productId, UpdateProductRequest request) {
        Product product = findById(productId);

        if (request.categoryNames() != null) {
            Set<Category> categories = categoryService.findOrCreateCategories(request.categoryNames());
            product.updateCategories(categories);
        }

        product.updateDetails(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.isActive()
        );

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto manageProductStock(Long productId, UpdateStockRequest request) {
        Product product = findById(productId);
        product.updateStock(request.stockQuantity());
        return ProductResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findById(productId);
        productRepository.delete(product);
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
