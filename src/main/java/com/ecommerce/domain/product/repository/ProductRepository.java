package com.ecommerce.domain.product.repository;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상태별 상품 조회
    List<Product> findByStatus(ProductStatus status);

    // 판매 가능한 상품 조회 (ACTIVE 상태이면서 재고가 있는 상품)
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0")
    List<Product> findAvailableProducts();

    // 브랜드별 상품 조회 (특정 상태)
    List<Product> findByBrandAndStatus(String brand, ProductStatus status);

    // 카테고리별 판매 가능한 상품 조회
    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.name = :categoryName AND p.status = 'ACTIVE' AND p.stockQuantity > 0")
    List<Product> findAvailableProductsByCategory(@Param("categoryName") String categoryName);

    // 품절된 상품 조회
    @Query("SELECT p FROM Product p WHERE p.status = 'OUT_OF_STOCK' OR p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();


    @Query("""
        SELECT p FROM Product p 
        LEFT JOIN p.category c 
        WHERE (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)
        AND (:category IS NULL OR c.name = :category)
        AND (:brand IS NULL OR p.brand = :brand)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:inStock IS NULL OR :inStock = false OR p.stockQuantity > 0)
        AND (:status IS NULL OR p.status = :status)
        """)
    Slice<Product> findProductsWithConditions(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

}