package com.ecommerce.api.v1.product.controller;

import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.product.dto.response.CategoryDto;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.ProductStatus;
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
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("[상품 등록][성공] - 카테고리와 이미지를 포함한 상품 등록")
    void addProduct_WithCategoryAndImages_Success() throws Exception {
        // given
        List<String> categoryNames = List.of("전자기기", "신상품");
        List<String> imageUrls = List.of(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg"
        );
        AddProductRequest request = new AddProductRequest(
                "새 상품", 
                "새 상품입니다", 
                "brand", 
                BigDecimal.valueOf(10000), 
                100, 
                imageUrls, 
                categoryNames
        );

        Set<CategoryDto> categoryDtos = Set.of(
                new CategoryDto(1L, "전자기기"),
                new CategoryDto(2L, "신상품")
        );

        ProductResponseDto savedProductDto = new ProductResponseDto(
                1L,
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.brand(),
                ProductStatus.PENDING,
                ProductStatus.PENDING.getDescription(),
                imageUrls,
                categoryDtos,
                false
        );

        when(productService.addProduct(any(AddProductRequest.class))).thenReturn(savedProductDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(savedProductDto.name())))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.statusDescription", is("승인대기")))
                .andExpect(jsonPath("$.data.imageUrls.length()", is(2)))
                .andExpect(jsonPath("$.data.imageUrls[0]", is("https://example.com/image1.jpg")))
                .andExpect(jsonPath("$.data.categories.length()", is(2)))
                .andExpect(jsonPath("$.message").value("상품 등록이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 등록][성공] - 이미지 없이 상품 등록")
    void addProduct_WithoutImages_Success() throws Exception {
        // given
        List<String> categoryNames = List.of("전자기기");
        AddProductRequest request = new AddProductRequest(
                "새 상품", 
                "새 상품입니다", 
                "brand", 
                BigDecimal.valueOf(10000), 
                100, 
                null, // 이미지 없음
                categoryNames
        );

        Set<CategoryDto> categoryDtos = Set.of(
                new CategoryDto(1L, "전자기기")
        );

        ProductResponseDto savedProductDto = new ProductResponseDto(
                1L,
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.brand(),
                ProductStatus.PENDING,
                ProductStatus.PENDING.getDescription(),
                List.of(), // 빈 이미지 리스트
                categoryDtos,
                false
        );

        when(productService.addProduct(any(AddProductRequest.class))).thenReturn(savedProductDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(savedProductDto.name())))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.imageUrls.length()", is(0)))
                .andExpect(jsonPath("$.message").value("상품 등록이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 수정][성공] - 카테고리와 이미지를 포함한 상품 수정")
    void updateProduct_WithCategoryAndImages_Success() throws Exception {
        // given
        List<String> categoryNames = List.of("의류", "시즌오프");
        List<String> imageUrls = List.of(
                "https://example.com/updated1.jpg",
                "https://example.com/updated2.jpg",
                "https://example.com/updated3.jpg"
        );
        UpdateProductRequest request = new UpdateProductRequest(
                "수정 상품", 
                "수정 설명", 
                "brand", 
                BigDecimal.valueOf(12000), 
                120, 
                imageUrls, 
                ProductStatus.ACTIVE, 
                categoryNames
        );

        Set<CategoryDto> categoryDtos = Set.of(
                new CategoryDto(3L, "의류"),
                new CategoryDto(4L, "시즌오프")
        );

        ProductResponseDto updatedProductDto = new ProductResponseDto(
                1L,
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.brand(),
                ProductStatus.ACTIVE,
                ProductStatus.ACTIVE.getDescription(),
                imageUrls,
                categoryDtos,
                true
        );

        when(productService.updateProduct(any(Long.class), any(UpdateProductRequest.class)))
                .thenReturn(updatedProductDto);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(updatedProductDto.name())))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.availableForSale", is(true)))
                .andExpect(jsonPath("$.data.imageUrls.length()", is(3)))
                .andExpect(jsonPath("$.data.categories.length()", is(2)))
                .andExpect(jsonPath("$.message").value("상품 수정이 완료되었습니다"));
    }

    @Test
    @DisplayName("[재고 수정][성공] - 재고 감소")
    void manageProductStock_Success() throws Exception {
        // given
        UpdateStockRequest stockRequest = new UpdateStockRequest(90);

        ProductResponseDto productDto = new ProductResponseDto(
                1L,
                "테스트 상품",
                "테스트 설명",
                BigDecimal.valueOf(10000),
                90,
                "테스트 브랜드",
                ProductStatus.ACTIVE,
                ProductStatus.ACTIVE.getDescription(),
                List.of("https://example.com/test.jpg"),
                Set.of(),
                true
        );

        when(productService.manageProductStock(any(Long.class), any(UpdateStockRequest.class)))
                .thenReturn(productDto);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}/stock", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(productDto.id().intValue())))
                .andExpect(jsonPath("$.data.name", is(productDto.name())))
                .andExpect(jsonPath("$.data.stockQuantity", is(90)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.message").value("재고 수정이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 삭제][성공] - 정상 삭제")
    void deleteProduct_Success() throws Exception {
        // given
        doNothing().when(productService).deleteProduct(1L);

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", 1L));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("상품 삭제가 완료되었습니다"));
    }
}