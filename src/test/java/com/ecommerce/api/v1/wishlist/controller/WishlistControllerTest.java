package com.ecommerce.api.v1.wishlist.controller;

import com.ecommerce.api.v1.wishlist.dto.request.AddWishlistItemRequest;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistItemDto;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistResponseDto;
import com.ecommerce.domain.user.service.WishlistService;
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
import java.time.LocalDateTime;
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
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    @Test
    @DisplayName("[위시리스트 조회][성공] - 사용자의 위시리스트 조회")
    @WithMockUser(username = "testuser")
    void getWishlist_Success() throws Exception {
        // given
        WishlistItemDto item = WishlistItemDto.builder()
                .productId(1L)
                .productName("Test Product")
                .price(BigDecimal.valueOf(10000))
                .imageUrl("http://example.com/image.jpg")
                .addedAt(LocalDateTime.now())
                .build();

        WishlistResponseDto responseDto = WishlistResponseDto.builder()
                .items(List.of(item))
                .totalItems(1)
                .build();

        when(wishlistService.getWishlist(eq("testuser"))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(1))
                .andExpect(jsonPath("$.data.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.data.items[0].price").value(10000))
                .andExpect(jsonPath("$.message").value("위시리스트 조회가 완료되었습니다"));
    }

    @Test
    @DisplayName("[위시리스트 상품 추가][성공] - 위시리스트에 상품 추가")
    @WithMockUser(username = "testuser")
    void addWishlistItem_Success() throws Exception {
        // given
        AddWishlistItemRequest request = new AddWishlistItemRequest(1L);
        doNothing().when(wishlistService).addWishlistItem(eq("testuser"), any(AddWishlistItemRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("위시리스트에 상품이 추가되었습니다"));
    }

    @Test
    @DisplayName("[위시리스트 상품 추가][실패] - 유효하지 않은 요청")
    @WithMockUser(username = "testuser")
    void addWishlistItem_InvalidRequest_Fail() throws Exception {
        // given
        AddWishlistItemRequest request = new AddWishlistItemRequest(null);

        // when & then
        mockMvc.perform(post("/api/v1/wishlist/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[위시리스트 상품 삭제][성공] - 위시리스트에서 상품 삭제")
    @WithMockUser(username = "testuser")
    void removeWishlistItem_Success() throws Exception {
        // given
        Long productId = 1L;
        doNothing().when(wishlistService).removeWishlistItem(eq("testuser"), eq(productId));

        // when & then
        mockMvc.perform(delete("/api/v1/wishlist/items/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("위시리스트에서 상품이 삭제되었습니다"));
    }
}