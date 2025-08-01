package com.ecommerce.api.v1.cart.controller;

import com.ecommerce.api.v1.cart.dto.request.AddCartItemRequest;
import com.ecommerce.api.v1.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.v1.cart.dto.response.CartResponseDto;
import com.ecommerce.domain.order.service.CartService;
import com.ecommerce.global.utils.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController implements CartApiSpecification {

    private final CartService cartService;

    @GetMapping("")
    public RsData<CartResponseDto> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        CartResponseDto cart = cartService.getCart(username);
        return RsData.success(HttpStatus.OK, cart, "장바구니 조회가 완료되었습니다");
    }

    @PostMapping("/items")
    public RsData<Void> addCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        String username = userDetails.getUsername();
        cartService.addCartItem(username, request);
        return RsData.success(HttpStatus.CREATED, null, "장바구니에 상품이 추가되었습니다");
    }

    @PutMapping("/items/{productId}")
    public RsData<Void> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        String username = userDetails.getUsername();
        cartService.updateCartItem(username, productId, request);
        return RsData.success(HttpStatus.OK, null, "장바구니 상품 수량이 수정되었습니다");
    }

    @DeleteMapping("/items/{productId}")
    public RsData<Void> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId
    ) {
        cartService.removeCartItem(userDetails.getUsername(), productId);
        return RsData.success(HttpStatus.OK, null, "장바구니에서 상품이 삭제되었습니다");
    }
}