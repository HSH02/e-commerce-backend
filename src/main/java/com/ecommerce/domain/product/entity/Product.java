package com.ecommerce.domain.product.entity;

import com.ecommerce.global.entity.BaseEntity;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> category = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void updateDetails(
            String name,
            String description,
            String brand,
            BigDecimal price,
            Integer stockQuantity,
            ProductStatus status
    ) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (brand != null) {
            this.brand = brand;
        }
        if (price != null) {
            this.price = price;
        }
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void updateStock(int quantity) {
        this.stockQuantity = quantity;
        if (this.stockQuantity < 0) {
            throw new ServiceException(ErrorCode.STOCK_CANNOT_MINUS);
        }
        
        // 재고가 0이 되면 품절 상태로 변경
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK && this.stockQuantity > 0) {
            // 품절 상태에서 재고가 다시 생기면 판매중으로 변경
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    public boolean isAvailableForSale() {
        return this.status == ProductStatus.ACTIVE && this.stockQuantity > 0;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void updateCategories(Set<Category> newCategories) {
        this.category.clear();
        if (newCategories != null) {
            this.category.addAll(newCategories);
        }
    }

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(category);
    }
}