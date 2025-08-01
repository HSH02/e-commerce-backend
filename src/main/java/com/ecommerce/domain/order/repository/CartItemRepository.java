package com.ecommerce.domain.order.repository;

import com.ecommerce.domain.order.entity.Cart;
import com.ecommerce.domain.order.entity.CartItem;
import com.ecommerce.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartAndProduct(Cart cart, Product product);
}