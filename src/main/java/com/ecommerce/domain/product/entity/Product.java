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

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

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

    public void updateDetails(
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            Boolean isActive
    ) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (stockQuantity != null) {
            this.stockQuantity = stockQuantity;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void updateStock(int quantity) {
        this.stockQuantity = quantity;
        if (this.stockQuantity < 0) {
            throw new ServiceException(ErrorCode.STOCK_CANNOT_MINUS);
        }
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
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
