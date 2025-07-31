package com.ecommerce.domain.product.repository;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductOrderByIsMainDescIdAsc(Product product);

    void deleteByProduct(Product product);

    List<ProductImage> findByProductId(Long productId);

}
