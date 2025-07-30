package com.ecommerce.domain.product.service;

import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.repository.ProductRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product addProduct(AddProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .stockQuantity(request.stockQuantity())
                .build();

        productRepository.save(product);

        return product;
    }


    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest request) {
        Product product = findById(productId);

        product.updateDetails(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.isActive()
        );

        return product;
    }

    @Transactional
    public Product manageProductStock(Long productId, UpdateStockRequest request) {
        Product product = findById(productId);
        product.updateStock(request.stockQuantity());
        return product;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findById(productId);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
