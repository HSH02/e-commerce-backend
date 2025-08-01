package com.ecommerce.api.v1.wishlist.controller;

import com.ecommerce.api.v1.wishlist.dto.request.AddWishlistItemRequest;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistResponseDto;
import com.ecommerce.domain.user.service.WishlistService;
import com.ecommerce.global.utils.dto.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistController implements WishlistApiSpecification {

    private final WishlistService wishlistService;

    @GetMapping("")
    public RsData<WishlistResponseDto> getWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        WishlistResponseDto wishlist = wishlistService.getWishlist(username);
        return RsData.success(HttpStatus.OK, wishlist, "위시리스트 조회가 완료되었습니다");
    }

    @PostMapping("/items")
    public RsData<Void> addWishlistItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddWishlistItemRequest request
    ) {
        String username = userDetails.getUsername();
        wishlistService.addWishlistItem(username, request);
        return RsData.success(HttpStatus.CREATED, null, "위시리스트에 상품이 추가되었습니다");
    }

    @DeleteMapping("/items/{productId}")
    public RsData<Void> removeWishlistItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId
    ) {
        String username = userDetails.getUsername();
        wishlistService.removeWishlistItem(username, productId);
        return RsData.success(HttpStatus.OK, null, "위시리스트에서 상품이 삭제되었습니다");
    }
}