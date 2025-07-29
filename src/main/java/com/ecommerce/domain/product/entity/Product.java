package com.ecommerce.domain.product.entity;

import com.ecommerce.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "product_category",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public void addCategory(Category category) {
        this.categories.add(category);
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);
    }

    public void updateStock(int quantity) {
        this.stockQuantity += quantity;
        if (this.stockQuantity < 0) {
            throw new IllegalStateException("재고는 0보다 작을 수 없습니다.");
        }
    }
}
