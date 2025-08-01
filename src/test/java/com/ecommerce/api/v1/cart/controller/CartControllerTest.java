package com.ecommerce.api.v1.cart.controller;

import com.ecommerce.api.v1.cart.dto.request.AddCartItemRequest;
import com.ecommerce.api.v1.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.v1.cart.dto.response.CartItemDto;
import com.ecommerce.api.v1.cart.dto.response.CartResponseDto;
import com.ecommerce.domain.order.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @Test
    @DisplayName("[장바구니 조회][성공] - 사용자의 장바구니 조회")
    @WithMockUser(username = "testuser")
    void getCart_Success() throws Exception {
        // given
        CartItemDto item = CartItemDto.builder()
                .productId(1L)
                .productName("Test Product")
                .price(BigDecimal.valueOf(10000))
                .quantity(2)
                .imageUrl("http://example.com/image.jpg")
                .subtotal(BigDecimal.valueOf(20000))
                .build();

        CartResponseDto responseDto = CartResponseDto.builder()
                .items(List.of(item))
                .totalItems(1)
                .totalAmount(BigDecimal.valueOf(20000))
                .build();

        when(cartService.getCart(eq("testuser"))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(20000))
                .andExpect(jsonPath("$.data.items[0].productId").value(1))
                .andExpect(jsonPath("$.data.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.data.items[0].price").value(10000))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].subtotal").value(20000))
                .andExpect(jsonPath("$.message").value("장바구니 조회가 완료되었습니다"));
    }

    @Test
    @DisplayName("[장바구니 상품 추가][성공] - 장바구니에 상품 추가")
    @WithMockUser(username = "testuser")
    void addCartItem_Success() throws Exception {
        // given
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);
        doNothing().when(cartService).addCartItem(eq("testuser"), any(AddCartItemRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("장바구니에 상품이 추가되었습니다"));
    }

    @Test
    @DisplayName("[장바구니 상품 추가][실패] - 유효하지 않은 요청")
    @WithMockUser(username = "testuser")
    void addCartItem_InvalidRequest_Fail() throws Exception {
        // given
        AddCartItemRequest request = new AddCartItemRequest(null, null);

        // when & then
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[장바구니 상품 수량 수정][성공] - 장바구니 상품 수량 수정")
    @WithMockUser(username = "testuser")
    void updateCartItem_Success() throws Exception {
        // given
        Long productId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);
        doNothing().when(cartService).updateCartItem(eq("testuser"), eq(productId), any(UpdateCartItemRequest.class));

        // when & then
        mockMvc.perform(put("/api/v1/cart/items/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("장바구니 상품 수량이 수정되었습니다"));
    }

    @Test
    @DisplayName("[장바구니 상품 수량 수정][실패] - 유효하지 않은 요청")
    @WithMockUser(username = "testuser")
    void updateCartItem_InvalidRequest_Fail() throws Exception {
        // given
        Long productId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(null);

        // when & then
        mockMvc.perform(put("/api/v1/cart/items/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[장바구니 상품 삭제][성공] - 장바구니에서 상품 삭제")
    @WithMockUser(username = "testuser")
    void removeCartItem_Success() throws Exception {
        // given
        Long productId = 1L;
        doNothing().when(cartService).removeCartItem(eq("testuser"), eq(productId));

        // when & then
        mockMvc.perform(delete("/api/v1/cart/items/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("장바구니에서 상품이 삭제되었습니다"));
    }


}