package com.ecommerce.api.v1.wishlist.controller;

import com.ecommerce.api.v1.wishlist.dto.request.AddWishlistItemRequest;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistResponseDto;
import com.ecommerce.global.utils.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "위시리스트", description = "위시리스트 관련 API")
public interface WishlistApiSpecification {

    @Operation(summary = "위시리스트 조회", description = "현재 사용자의 위시리스트에 담긴 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "위시리스트 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    RsData<WishlistResponseDto> getWishlist(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(summary = "위시리스트에 상품 추가", description = "현재 사용자의 위시리스트에 상품을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "위시리스트에 상품 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> addWishlistItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddWishlistItemRequest request
    );

    @Operation(summary = "위시리스트에서 상품 삭제", description = "현재 사용자의 위시리스트에서 상품을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "위시리스트에서 상품 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> removeWishlistItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "상품 ID") @PathVariable Long productId
    );
}