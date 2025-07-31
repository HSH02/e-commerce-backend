package com.ecommerce.api.v1.product.controller;

import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductStatusRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.product.dto.response.CategoryDto;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.global.utils.dto.SliceResponseDto;
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

    @Test
    @DisplayName("[상품 상태 수정][성공] - ACTIVE에서 INACTIVE로 변경")
    void manageProductStatus_ActiveToInactive_Success() throws Exception {
        // given
        UpdateProductStatusRequest statusRequest = new UpdateProductStatusRequest(ProductStatus.INACTIVE);

        ProductResponseDto productDto = new ProductResponseDto(
                1L,
                "테스트 상품",
                "테스트 설명",
                BigDecimal.valueOf(10000),
                100,
                "테스트 브랜드",
                ProductStatus.INACTIVE,
                ProductStatus.INACTIVE.getDescription(),
                List.of("https://example.com/test.jpg"),
                Set.of(new CategoryDto(1L, "전자기기")),
                false // INACTIVE 상태이므로 판매 불가
        );

        when(productService.manageProductStatus(any(Long.class), any(UpdateProductStatusRequest.class)))
                .thenReturn(productDto);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(productDto.id().intValue())))
                .andExpect(jsonPath("$.data.name", is(productDto.name())))
                .andExpect(jsonPath("$.data.status", is("INACTIVE")))
                .andExpect(jsonPath("$.data.statusDescription", is("판매중지")))
                .andExpect(jsonPath("$.data.availableForSale", is(false)))
                .andExpect(jsonPath("$.message").value("상태 수정이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 상태 수정][성공] - PENDING에서 ACTIVE로 승인")
    void manageProductStatus_PendingToActive_Success() throws Exception {
        // given
        UpdateProductStatusRequest statusRequest = new UpdateProductStatusRequest(ProductStatus.ACTIVE);

        ProductResponseDto productDto = new ProductResponseDto(
                2L,
                "승인된 상품",
                "승인 완료된 상품입니다",
                BigDecimal.valueOf(25000),
                50,
                "승인브랜드",
                ProductStatus.ACTIVE,
                ProductStatus.ACTIVE.getDescription(),
                List.of("https://example.com/approved.jpg"),
                Set.of(new CategoryDto(2L, "의류")),
                true // ACTIVE 상태이고 재고가 있으므로 판매 가능
        );

        when(productService.manageProductStatus(any(Long.class), any(UpdateProductStatusRequest.class)))
                .thenReturn(productDto);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}/status", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(productDto.id().intValue())))
                .andExpect(jsonPath("$.data.name", is(productDto.name())))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.statusDescription", is("판매중")))
                .andExpect(jsonPath("$.data.availableForSale", is(true)))
                .andExpect(jsonPath("$.message").value("상태 수정이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 검색][성공] - 키워드와 카테고리로 검색")
    void searchProduct_WithKeywordAndCategory_Success() throws Exception {
        // given
        ProductResponseDto product1 = new ProductResponseDto(
                1L, "청바지", "편안한 청바지입니다", BigDecimal.valueOf(89000), 
                10, "데님브랜드", ProductStatus.ACTIVE, "판매중",
                List.of("https://example.com/jeans1.jpg"),
                Set.of(new CategoryDto(1L, "의류")), true
        );
        
        ProductResponseDto product2 = new ProductResponseDto(
                2L, "청자켓", "스타일리시한 청자켓", BigDecimal.valueOf(129000), 
                5, "데님브랜드", ProductStatus.ACTIVE, "판매중",
                List.of("https://example.com/jacket1.jpg"),
                Set.of(new CategoryDto(1L, "의류")), true
        );

        SliceResponseDto<ProductResponseDto> sliceResponse = SliceResponseDto.<ProductResponseDto>builder()
                .content(List.of(product1, product2))
                .currentPage(0)
                .size(10)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .numberOfElements(2)
                .build();

        when(productService.searchProductsForInfiniteScroll(any(), any()))
                .thenReturn(sliceResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "청")
                .param("category", "의류")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content.length()", is(2)))
                .andExpect(jsonPath("$.data.content[0].name", is("청바지")))
                .andExpect(jsonPath("$.data.content[1].name", is("청자켓")))
                .andExpect(jsonPath("$.data.currentPage", is(0)))
                .andExpect(jsonPath("$.data.size", is(10)))
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.numberOfElements", is(2)))
                .andExpect(jsonPath("$.message").value("상품 검색이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 검색][성공] - 가격 범위로 검색")
    void searchProduct_WithPriceRange_Success() throws Exception {
        // given
        ProductResponseDto expensiveProduct = new ProductResponseDto(
                3L, "명품 가방", "고급 가죽 가방", BigDecimal.valueOf(450000), 
                3, "럭셔리브랜드", ProductStatus.ACTIVE, "판매중",
                List.of("https://example.com/bag1.jpg"),
                Set.of(new CategoryDto(3L, "가방")), true
        );

        SliceResponseDto<ProductResponseDto> sliceResponse = SliceResponseDto.<ProductResponseDto>builder()
                .content(List.of(expensiveProduct))
                .currentPage(0)
                .size(10)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .numberOfElements(1)
                .build();

        when(productService.searchProductsForInfiniteScroll(any(), any()))
                .thenReturn(sliceResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("minPrice", "400000")
                .param("maxPrice", "500000")
                .param("page", "0")
                .param("size", "10"));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content.length()", is(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("명품 가방")))
                .andExpect(jsonPath("$.data.content[0].price", is(450000)))
                .andExpect(jsonPath("$.message").value("상품 검색이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 검색][성공] - 빈 결과 반환")
    void searchProduct_EmptyResult_Success() throws Exception {
        // given
        SliceResponseDto<ProductResponseDto> emptySliceResponse = SliceResponseDto.empty();

        when(productService.searchProductsForInfiniteScroll(any(), any()))
                .thenReturn(emptySliceResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "존재하지않는상품")
                .param("page", "0")
                .param("size", "10"));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content.length()", is(0)))
                .andExpect(jsonPath("$.data.numberOfElements", is(0)))
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.isFirst", is(true)))
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.message").value("상품 검색이 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 상세 조회][성공] - 존재하는 상품 조회")
    void searchProductDetail_ExistingProduct_Success() throws Exception {
        // given
        ProductResponseDto productDetail = new ProductResponseDto(
                5L,
                "상세 조회 테스트 상품",
                "이 상품은 상세 조회 테스트용 상품입니다. 다양한 정보를 포함하고 있습니다.",
                BigDecimal.valueOf(75000),
                25,
                "테스트브랜드",
                ProductStatus.ACTIVE,
                ProductStatus.ACTIVE.getDescription(),
                List.of(
                    "https://example.com/detail1.jpg",
                    "https://example.com/detail2.jpg",
                    "https://example.com/detail3.jpg"
                ),
                Set.of(
                    new CategoryDto(1L, "전자기기"),
                    new CategoryDto(4L, "신상품")
                ),
                true
        );

        when(productService.findProductResponseById(5L)).thenReturn(productDetail);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", 5L));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(productDetail.id().intValue())))
                .andExpect(jsonPath("$.data.name", is(productDetail.name())))
                .andExpect(jsonPath("$.data.description", is(productDetail.description())))
                .andExpect(jsonPath("$.data.price", is(productDetail.price().intValue())))
                .andExpect(jsonPath("$.data.stockQuantity", is(productDetail.stockQuantity())))
                .andExpect(jsonPath("$.data.brand", is(productDetail.brand())))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.statusDescription", is("판매중")))
                .andExpect(jsonPath("$.data.imageUrls.length()", is(3)))
                .andExpect(jsonPath("$.data.categories.length()", is(2)))
                .andExpect(jsonPath("$.data.availableForSale", is(true)))
                .andExpect(jsonPath("$.message").value("상품 조회가 완료되었습니다"));
    }

    @Test
    @DisplayName("[상품 상세 조회][성공] - 품절 상품 조회")
    void searchProductDetail_OutOfStockProduct_Success() throws Exception {
        // given
        ProductResponseDto outOfStockProduct = new ProductResponseDto(
                6L,
                "품절 상품",
                "현재 품절된 상품입니다",
                BigDecimal.valueOf(99000),
                0, // 재고 0
                "품절브랜드",
                ProductStatus.OUT_OF_STOCK,
                ProductStatus.OUT_OF_STOCK.getDescription(),
                List.of("https://example.com/outofstock.jpg"),
                Set.of(new CategoryDto(2L, "의류")),
                false // 품절이므로 판매 불가
        );

        when(productService.findProductResponseById(6L)).thenReturn(outOfStockProduct);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", 6L));

        // then
        result
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(outOfStockProduct.id().intValue())))
                .andExpect(jsonPath("$.data.name", is(outOfStockProduct.name())))
                .andExpect(jsonPath("$.data.stockQuantity", is(0)))
                .andExpect(jsonPath("$.data.status", is("OUT_OF_STOCK")))
                .andExpect(jsonPath("$.data.statusDescription", is("품절")))
                .andExpect(jsonPath("$.data.availableForSale", is(false)))
                .andExpect(jsonPath("$.message").value("상품 조회가 완료되었습니다"));
    }
}