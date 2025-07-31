package com.ecommerce.domain.product.service;

import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.repository.ProductImageRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductImageService productImageService;

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
    @DisplayName("[createProductImages][성공] - 다중 이미지 URL로 상품 이미지 생성")
    void createProductImages_MultipleImages_Success() {
        // given
        List<String> imageUrls = List.of(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
        );

        List<ProductImage> savedImages = List.of(
                createMockProductImage(1L, "https://example.com/image1.jpg", true),
                createMockProductImage(2L, "https://example.com/image2.jpg", false),
                createMockProductImage(3L, "https://example.com/image3.jpg", false)
        );

        when(productImageRepository.saveAll(anyList())).thenReturn(savedImages);

        // when
        List<ProductImage> result = productImageService.createProductImages(product, imageUrls);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).isMain()).isTrue();
        assertThat(result.get(1).isMain()).isFalse();
        assertThat(result.get(2).isMain()).isFalse();

        verify(productImageRepository).saveAll(argThat(images -> {
            List<ProductImage> imageList = (List<ProductImage>) images;
            return imageList.size() == 3 &&
                    imageList.get(0).isMain() &&
                    !imageList.get(1).isMain() &&
                    !imageList.get(2).isMain() &&
                    imageList.get(0).getImageUrl().equals("https://example.com/image1.jpg");
        }));
    }

    @Test
    @DisplayName("[createProductImages][성공] - 단일 이미지 URL로 상품 이미지 생성")
    void createProductImages_SingleImage_Success() {
        // given
        List<String> imageUrls = List.of("https://example.com/single-image.jpg");

        List<ProductImage> savedImages = List.of(
                createMockProductImage(1L, "https://example.com/single-image.jpg", true)
        );

        when(productImageRepository.saveAll(anyList())).thenReturn(savedImages);

        // when
        List<ProductImage> result = productImageService.createProductImages(product, imageUrls);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isMain()).isTrue();
        assertThat(result.getFirst().getImageUrl()).isEqualTo("https://example.com/single-image.jpg");

        verify(productImageRepository).saveAll(argThat(images -> {
            List<ProductImage> imageList = (List<ProductImage>) images;
            return imageList.size() == 1 &&
                    imageList.getFirst().isMain() &&
                    imageList.getFirst().getProduct().equals(product);
        }));
    }

    @Test
    @DisplayName("[createProductImages][성공] - 빈 이미지 URL 리스트 처리")
    void createProductImages_EmptyList_ReturnsEmptyList() {
        // given
        List<String> imageUrls = Collections.emptyList();

        // when
        List<ProductImage> result = productImageService.createProductImages(product, imageUrls);

        // then
        assertThat(result).isEmpty();
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("[createProductImages][성공] - null 이미지 URL 리스트 처리")
    void createProductImages_NullList_ReturnsEmptyList() {
        // when
        List<ProductImage> result = productImageService.createProductImages(product, null);

        // then
        assertThat(result).isEmpty();
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("[updateProductImages][성공] - 기존 이미지 삭제 후 새 이미지 생성")
    void updateProductImages_WithNewImages_Success() {
        // given
        List<String> newImageUrls = List.of(
                "https://example.com/new1.jpg",
                "https://example.com/new2.jpg"
        );

        List<ProductImage> newImages = List.of(
                createMockProductImage(4L, "https://example.com/new1.jpg", true),
                createMockProductImage(5L, "https://example.com/new2.jpg", false)
        );

        doNothing().when(productImageRepository).deleteByProduct(product);
        when(productImageRepository.saveAll(anyList())).thenReturn(newImages);

        // when
        productImageService.updateProductImages(product, newImageUrls);

        // then
        verify(productImageRepository).deleteByProduct(product);
        verify(productImageRepository).saveAll(argThat(images -> {
            List<ProductImage> imageList = (List<ProductImage>) images;
            return imageList.size() == 2 &&
                    imageList.get(0).isMain() &&
                    !imageList.get(1).isMain();
        }));
    }

    @Test
    @DisplayName("[updateProductImages][성공] - 기존 이미지 삭제만 (빈 리스트)")
    void updateProductImages_WithEmptyList_OnlyDeletesExisting() {
        // given
        List<String> emptyImageUrls = Collections.emptyList();

        doNothing().when(productImageRepository).deleteByProduct(product);

        // when
        productImageService.updateProductImages(product, emptyImageUrls);

        // then
        verify(productImageRepository).deleteByProduct(product);
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("[updateProductImages][성공] - 기존 이미지 삭제만 (null)")
    void updateProductImages_WithNull_OnlyDeletesExisting() {
        // given
        doNothing().when(productImageRepository).deleteByProduct(product);

        // when
        productImageService.updateProductImages(product, null);

        // then
        verify(productImageRepository).deleteByProduct(product);
        verify(productImageRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("[createProductImages][검증] - ProductImage 객체가 올바르게 생성되는지 확인")
    void createProductImages_ValidatesProductImageCreation() {
        // given
        List<String> imageUrls = List.of("https://example.com/test.jpg");

        List<ProductImage> savedImages = List.of(
                createMockProductImage(1L, "https://example.com/test.jpg", true)
        );

        when(productImageRepository.saveAll(anyList())).thenReturn(savedImages);

        // when
        productImageService.createProductImages(product, imageUrls);

        // then
        verify(productImageRepository).saveAll(argThat(images -> {
            List<ProductImage> imageList = (List<ProductImage>) images;
            ProductImage firstImage = imageList.getFirst();

            return firstImage.getProduct().equals(product) &&
                    firstImage.getImageUrl().equals("https://example.com/test.jpg") &&
                    firstImage.isMain();
        }));
    }

    private ProductImage createMockProductImage(Long id, String imageUrl, boolean isMain) {
        return ProductImage.builder()
                .id(id)
                .product(product)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .build();
    }
}