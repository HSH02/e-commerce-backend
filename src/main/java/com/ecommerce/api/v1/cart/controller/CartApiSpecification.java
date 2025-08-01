package com.ecommerce.api.v1.cart.controller;

import com.ecommerce.api.v1.cart.dto.request.AddCartItemRequest;
import com.ecommerce.api.v1.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.v1.cart.dto.response.CartResponseDto;
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

@Tag(name = "장바구니", description = "장바구니 관련 API")
public interface CartApiSpecification {

    @Operation(summary = "장바구니 조회", description = "현재 사용자의 장바구니에 담긴 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    RsData<CartResponseDto> getCart(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(summary = "장바구니에 상품 추가", description = "현재 사용자의 장바구니에 상품을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "장바구니에 상품 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> addCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddCartItemRequest request
    );

    @Operation(summary = "장바구니 상품 수량 수정", description = "현재 사용자의 장바구니에 담긴 상품의 수량을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니 상품 수량 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    );

    @Operation(summary = "장바구니에서 상품 삭제", description = "현재 사용자의 장바구니에서 상품을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "장바구니에서 상품 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    RsData<Void> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "상품 ID") @PathVariable Long productId
    );
}