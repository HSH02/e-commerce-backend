package com.ecommerce.domain.order.entity;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
