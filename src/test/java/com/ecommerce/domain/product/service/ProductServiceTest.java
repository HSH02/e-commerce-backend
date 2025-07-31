package com.ecommerce.domain.product.service;

import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.product.dto.request.UpdateStockRequest;
import com.ecommerce.api.v1.product.dto.response.ProductResponseDto;
import com.ecommerce.domain.product.entity.Category;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.repository.ProductRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(BigDecimal.valueOf(10_000L))
                .stockQuantity(100)
                .brand("테스트 브랜드")
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("[addProduct][성공] - 이미지가 있는 상품 등록")
    void addProduct_WithImages_Success() {
        // given
        List<String> categoryNames = List.of("테스트");
        List<String> imageUrls = List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg");
        AddProductRequest request = new AddProductRequest(
                "테스트 상품", 
                "설명 입니다", 
                "브랜드", 
                BigDecimal.valueOf(10_000L), 
                100, 
                imageUrls, 
                categoryNames
        );
        
        when(categoryService.findOrCreateCategories(categoryNames)).thenReturn(Collections.emptySet());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productImageService.createProductImages(eq(product), eq(imageUrls)))
                .thenReturn(createMockProductImages(imageUrls));

        // when
        ProductResponseDto saved = productService.addProduct(request);

        // then
        assertThat(saved.name()).isEqualTo(request.name());
        assertThat(saved.price()).isEqualTo(request.price());
        assertThat(saved.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).save(any(Product.class));
        verify(categoryService).findOrCreateCategories(categoryNames);
        verify(productImageService).createProductImages(eq(product), eq(imageUrls));
    }

    @Test
    @DisplayName("[addProduct][성공] - 이미지가 없는 상품 등록")
    void addProduct_WithoutImages_Success() {
        // given
        List<String> categoryNames = List.of("테스트");
        AddProductRequest request = new AddProductRequest(
                "테스트 상품", 
                "설명 입니다", 
                "브랜드", 
                BigDecimal.valueOf(10_000L), 
                100, 
                null, // 이미지 없음
                categoryNames
        );
        
        when(categoryService.findOrCreateCategories(categoryNames)).thenReturn(Collections.emptySet());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        ProductResponseDto saved = productService.addProduct(request);

        // then
        assertThat(saved.name()).isEqualTo(request.name());
        assertThat(saved.price()).isEqualTo(request.price());
        assertThat(saved.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).save(any(Product.class));
        verify(categoryService).findOrCreateCategories(categoryNames);
        verify(productImageService, never()).createProductImages(any(), any());
    }

    @Test
    @DisplayName("[addProduct][성공] - 상품 등록 및 카테고리 추가")
    void addProductWithCategory_Success() {
        // given
        Category testCategory = Category.builder().id(1L).name("테스트").build();
        List<String> categoryNames = List.of("테스트");
        List<String> imageUrls = List.of("https://example.com/image.jpg");
        AddProductRequest request = new AddProductRequest(
                "테스트 상품", 
                "설명 입니다", 
                "브랜드", 
                BigDecimal.valueOf(10_000L), 
                100, 
                imageUrls, 
                categoryNames
        );
        
        when(categoryService.findOrCreateCategories(categoryNames)).thenReturn(Set.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productImageService.createProductImages(eq(product), eq(imageUrls)))
                .thenReturn(createMockProductImages(imageUrls));

        // when
        ProductResponseDto saved = productService.addProduct(request);

        // then
        assertThat(saved.name()).isEqualTo(request.name());
        assertThat(saved.price()).isEqualTo(request.price());
        assertThat(saved.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).save(any(Product.class));
        verify(categoryService).findOrCreateCategories(categoryNames);
        verify(productImageService).createProductImages(eq(product), eq(imageUrls));
    }

    @Test
    @DisplayName("[updateProduct][성공] - 이미지를 포함한 상품 수정")
    void updateProduct_WithImages_Success() {
        // given
        List<String> imageUrls = List.of("https://example.com/updated.jpg");
        UpdateProductRequest req = new UpdateProductRequest(
                "수정 상품", 
                "수정 설명", 
                "브랜드", 
                BigDecimal.valueOf(12_000L), 
                120, 
                imageUrls, 
                ProductStatus.ACTIVE,
                Collections.emptyList()
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productImageService).updateProductImages(eq(product), eq(imageUrls));

        // when
        ProductResponseDto updated = productService.updateProduct(1L, req);

        // then
        assertThat(updated.name()).isEqualTo(req.name());
        assertThat(updated.price()).isEqualTo(req.price());
        assertThat(updated.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(productImageService).updateProductImages(eq(product), eq(imageUrls));
    }

    @Test
    @DisplayName("[updateProduct][성공] - 이미지 업데이트 없이 상품 수정")
    void updateProduct_WithoutImageUpdate_Success() {
        // given
        UpdateProductRequest req = new UpdateProductRequest(
                "수정 상품", 
                "수정 설명", 
                "브랜드", 
                BigDecimal.valueOf(12_000L), 
                120, 
                null, // 이미지 업데이트 없음
                ProductStatus.INACTIVE,
                Collections.emptyList()
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        ProductResponseDto updated = productService.updateProduct(1L, req);

        // then
        assertThat(updated.name()).isEqualTo(req.name());
        assertThat(updated.price()).isEqualTo(req.price());
        verify(productImageService, never()).updateProductImages(any(), any());
    }

    @Test
    @DisplayName("[updateProduct][성공] - 카테고리와 이미지를 포함한 상품 수정")
    void updateProductWithCategoryAndImages_Success() {
        // given
        List<String> categoryNames = List.of("수정 카테고리", "인기상품");
        List<String> imageUrls = List.of("https://example.com/new1.jpg", "https://example.com/new2.jpg");
        Set<Category> categories = Set.of(
                Category.builder().id(2L).name("수정 카테고리").build(),
                Category.builder().id(3L).name("인기상품").build()
        );
        UpdateProductRequest req = new UpdateProductRequest(
                "수정 상품", 
                "수정 설명", 
                "브랜드", 
                BigDecimal.valueOf(12_000L), 
                120, 
                imageUrls, 
                ProductStatus.ACTIVE,
                categoryNames
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.findOrCreateCategories(categoryNames)).thenReturn(categories);
        doNothing().when(productImageService).updateProductImages(eq(product), eq(imageUrls));

        // when
        ProductResponseDto updated = productService.updateProduct(1L, req);

        // then
        assertThat(updated.name()).isEqualTo(req.name());
        assertThat(updated.price()).isEqualTo(req.price());
        assertThat(updated.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(categoryService).findOrCreateCategories(categoryNames);
        verify(productImageService).updateProductImages(eq(product), eq(imageUrls));
    }

    @Test
    @DisplayName("[manageProductStock][성공] - 재고 수정")
    void manageStock_Success() {
        // given
        UpdateStockRequest req = new UpdateStockRequest(80);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        ProductResponseDto updated = productService.manageProductStock(1L, req);

        // then
        assertThat(updated.id()).isEqualTo(product.getId());
        assertThat(updated.name()).isEqualTo(product.getName());
        assertThat(updated.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("[updateProductStatus][성공] - 상품 상태 변경")
    void updateProductStatus_Success() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        ProductResponseDto updated = productService.updateProductStatus(1L, ProductStatus.INACTIVE);

        // then
        assertThat(updated.id()).isEqualTo(product.getId());
        assertThat(updated.name()).isEqualTo(product.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("[deleteProduct][성공] - 상품 삭제")
    void deleteProduct_Success() {
        // given
        Product product = Product.builder()
                .id(1L)
                .name("테스트 상품")
                .description("테스트 설명")
                .price(BigDecimal.valueOf(10000))
                .stockQuantity(100)
                .brand("테스트브랜드")
                .status(ProductStatus.ACTIVE)
                .isDeleted(false).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        productService.deleteProduct(1L);

        // then
        verify(productRepository).findById(1L);
        assertThat(product.getIsDeleted()).isTrue();

    }

    @Test
    @DisplayName("[findById][실패] - 존재하지 않는 상품")
    void findProductEntityById_Fail_NotFound() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> productService.findProductEntityById(1L));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo("해당 상품을 찾을 수 없습니다.");
    }

    private List<ProductImage> createMockProductImages(List<String> imageUrls) {
        return imageUrls.stream()
                .map(url -> ProductImage.builder()
                        .id(1L)
                        .product(product)
                        .imageUrl(url)
                        .isMain(imageUrls.indexOf(url) == 0)
                        .build())
                .toList();
    }
}