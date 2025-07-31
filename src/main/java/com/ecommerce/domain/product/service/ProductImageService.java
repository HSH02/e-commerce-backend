package com.ecommerce.domain.product.service;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import com.ecommerce.domain.product.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductImageRepository productImageRepository;

    @Transactional
    public List<ProductImage> createProductImages(Product product, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductImage> productImages = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrls.get(i))
                    .isMain(i == 0) // 첫 번째 이미지를 메인으로 설정
                    .build();

            productImages.add(productImage);
        }

        return productImageRepository.saveAll(productImages);
    }

    @Transactional
    public void updateProductImages(Product product, List<String> imageUrls) {
        productImageRepository.deleteByProduct(product);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            createProductImages(product, imageUrls);
        }
    }

}