package com.ecommerce.domain.product.entity;

import com.ecommerce.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String imageUrl;

    private String altText;

    @Column(nullable = false)
    private boolean isMain;

    public void setProduct(Product product) {
        this.product = product;
    }
}
