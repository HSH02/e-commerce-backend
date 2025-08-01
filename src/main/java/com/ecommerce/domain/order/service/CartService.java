package com.ecommerce.domain.order.service;

import com.ecommerce.api.v1.cart.dto.request.AddCartItemRequest;
import com.ecommerce.api.v1.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.v1.cart.dto.response.CartItemDto;
import com.ecommerce.api.v1.cart.dto.response.CartResponseDto;
import com.ecommerce.domain.order.entity.Cart;
import com.ecommerce.domain.order.entity.CartItem;
import com.ecommerce.domain.order.repository.CartItemRepository;
import com.ecommerce.domain.order.repository.CartRepository;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    /**
     * 사용자의 장바구니를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 장바구니 응답 DTO
     */
    public CartResponseDto getCart(String email) {
        User user = userService.findByEmail(email);
        Cart cart = getOrCreateCart(user);
        
        List<CartItemDto> items = cart.getCartItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        BigDecimal totalAmount = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponseDto.builder()
                .items(items)
                .totalItems(items.size())
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 장바구니에 상품을 추가합니다.
     *
     * @param email 사용자 이메일
     * @param request 장바구니 상품 추가 요청 DTO
     */
    @Transactional
    public void addCartItem(String email, AddCartItemRequest request) {
        User user = userService.findByEmail(email);
        Product product = productService.findProductEntityById(request.productId());
        Cart cart = getOrCreateCart(user);
        
        // 이미 장바구니에 존재하는지 확인
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        
        if (existingItem != null) {
            // 이미 존재하면 수량 증가
            existingItem.updateQuantity(existingItem.getQuantity() + request.quantity());
        } else {
            // 새로운 아이템 추가
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }
    }

    /**
     * 장바구니 상품의 수량을 수정합니다.
     *
     * @param email 사용자 이메일
     * @param productId 상품 ID
     * @param request 장바구니 상품 수량 수정 요청 DTO
     * @throws ServiceException 장바구니에 존재하지 않는 상품인 경우
     */
    @Transactional
    public void updateCartItem(String email, Long productId, UpdateCartItemRequest request) {
        User user = userService.findByEmail(email);
        Product product = productService.findProductEntityById(productId);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        
        cartItem.updateQuantity(request.quantity());
    }

    /**
     * 장바구니에서 상품을 삭제합니다.
     *
     * @param email 사용자 이메일
     * @param productId 상품 ID
     * @throws ServiceException 장바구니에 존재하지 않는 상품인 경우
     */
    @Transactional
    public void removeCartItem(String email, Long productId) {
        User user = userService.findByEmail(email);
        Product product = productService.findProductEntityById(productId);
        Cart cart = getOrCreateCart(user);
        
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
    }
    
    /**
     * 사용자의 장바구니를 조회하거나 없으면 새로 생성합니다.
     *
     * @param user 사용자
     * @return 장바구니
     */
    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .cartItems(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }
    
    /**
     * CartItem 엔티티를 CartItemDto로 변환합니다.
     *
     * @param cartItem CartItem 엔티티
     * @return CartItemDto
     */
    private CartItemDto convertToDto(CartItem cartItem) {
        Product product = cartItem.getProduct();
        
        // 상품 이미지가 있는 경우 첫 번째 이미지를 사용
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getImageUrl();
        }
        
        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .imageUrl(imageUrl)
                .subtotal(subtotal)
                .build();
    }
}