package com.ecommerce.api.v1.admin.controller;

import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AddProductRequest addRequest;
    private UpdateProductRequest updateRequest;
    private UpdateStockRequest stockRequest;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();

        addRequest = new AddProductRequest("새 상품", "새 상품입니다", BigDecimal.valueOf(10000), 100);
        updateRequest = new UpdateProductRequest("수정 상품", "수정 설명", BigDecimal.valueOf(12000), 120, null, true);
        stockRequest = new UpdateStockRequest(90);

        savedProduct = Product.builder()
                .id(1L)
                .name(addRequest.name())
                .price(addRequest.price())
                .description(addRequest.description())
                .stockQuantity(addRequest.stockQuantity())
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("[상품 등록][성공] - 유효한 정보로 등록")
    void addProduct_Success() throws Exception {
        // given
        when(productService.addProduct(any(AddProductRequest.class))).thenReturn(savedProduct);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)));

        // then
        result
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(savedProduct.getName())))
                .andExpect(jsonPath("$.message").value("상품 등록이 완료되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("[상품 수정][성공] - 유효한 정보로 수정")
    void updateProduct_Success() throws Exception {
        // given
        when(productService.updateProduct(any(Long.class), any(UpdateProductRequest.class)))
                .thenReturn(savedProduct);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/products/{productId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(savedProduct.getName())))
                .andExpect(jsonPath("$.message").value("상품 수정이 완료되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("[재고 수정][성공] - 재고 감소")
    void manageProductStock_Success() throws Exception {
        // given
        savedProduct.updateStock(stockRequest.stockQuantity());
        when(productService.manageProductStock(any(Long.class), any(UpdateStockRequest.class)))
                .thenReturn(savedProduct);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/admin/products/{productId}/stock", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)));

        // then
        result
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.stockQuantity", is(stockRequest.stockQuantity())))
                .andExpect(jsonPath("$.message").value("재고 수정이 완료되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("[상품 삭제][성공] - 정상 삭제")
    void deleteProduct_Success() throws Exception {
        // given
        doNothing().when(productService).deleteProduct(1L);

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/admin/products/{productId}", 1L));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("상품 삭제가 완료되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}