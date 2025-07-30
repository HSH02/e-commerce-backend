package com.ecommerce.domain.product.service;

import com.ecommerce.api.v1.admin.dto.request.AddProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateProductRequest;
import com.ecommerce.api.v1.admin.dto.request.UpdateStockRequest;
import com.ecommerce.domain.product.entity.Product;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

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
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("[addProduct][성공] - 상품 등록")
    void addProduct_Success() {
        // given
        AddProductRequest request = new AddProductRequest("테스트 상품", "설명 입니다", BigDecimal.valueOf(10_000L), 100);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        Product saved = productService.addProduct(request);

        // then
        assertThat(saved.getName()).isEqualTo(request.name());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("[updateProduct][성공] - 상품 수정")
    void updateProduct_Success() {
        // given
        UpdateProductRequest req = new UpdateProductRequest("수정 상품", "수정 설명", BigDecimal.valueOf(10_000L), 120, null, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        Product updated = productService.updateProduct(1L, req);

        // then
        assertThat(updated.getName()).isEqualTo(req.name());
        assertThat(updated.getPrice()).isEqualTo(req.price());
    }

    @Test
    @DisplayName("[manageProductStock][성공] - 재고 수정")
    void manageStock_Success() {
        // given
        UpdateStockRequest req = new UpdateStockRequest(80);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        Product updated = productService.manageProductStock(1L, req);

        // then
        assertThat(updated.getStockQuantity()).isEqualTo(req.stockQuantity());
    }

    @Test
    @DisplayName("[deleteProduct][성공] - 상품 삭제")
    void deleteProduct_Success() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        // when
        productService.deleteProduct(1L);

        // then
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("[findById][실패] - 존재하지 않는 상품")
    void findById_Fail_NotFound() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> productService.findById(1L));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());
        assertThat(exception.getMessage()).isEqualTo("해당 상품을 찾을 수 없습니다.");
    }
}