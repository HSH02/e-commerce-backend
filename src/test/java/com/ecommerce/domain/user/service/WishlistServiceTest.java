package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.wishlist.dto.request.AddWishlistItemRequest;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistItemDto;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistResponseDto;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.entity.Wishlist;
import com.ecommerce.domain.user.repository.WishlistRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private WishlistService wishlistService;

    private User testUser;
    private Product testProduct;
    private Wishlist testWishlistItem;
    private ProductImage testProductImage;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .role(UserRole.USER)
                .build();

        // Create product with images
        List<ProductImage> images = new ArrayList<>();
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(10000))
                .description("Test Description")
                .stockQuantity(100)
                .status(ProductStatus.ACTIVE)
                .images(images)
                .build();
                
        testProductImage = ProductImage.builder()
                .id(1L)
                .product(testProduct)
                .imageUrl("http://example.com/image.jpg")
                .build();
                
        // Add image to the product's image list
        testProduct.getImages().add(testProductImage);

        testCreatedAt = LocalDateTime.now();
        
        testWishlistItem = Wishlist.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .build();
        
        // Use reflection to set createdAt field for testing
        try {
            var createdAtField = testWishlistItem.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testWishlistItem, testCreatedAt);
        } catch (Exception e) {
            // Ignore exception in test setup
        }
    }

    @Test
    @DisplayName("[getWishlist][성공] - 위시리스트 조회")
    void getWishlist_Success() {
        // given
        String email = "test@example.com";
        List<Wishlist> wishlistItems = List.of(testWishlistItem);

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(wishlistItems);

        // when
        WishlistResponseDto result = wishlistService.getWishlist(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        
        WishlistItemDto item = result.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(testProduct.getId());
        assertThat(item.getProductName()).isEqualTo(testProduct.getName());
        assertThat(item.getPrice()).isEqualTo(testProduct.getPrice());
        assertThat(item.getImageUrl()).isEqualTo(testProductImage.getImageUrl());
        assertThat(item.getAddedAt()).isEqualTo(testCreatedAt);

        verify(userService).findByEmail(email);
        verify(wishlistRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("[addWishlistItem][성공] - 위시리스트 상품 추가")
    void addWishlistItem_Success() {
        // given
        String email = "test@example.com";
        AddWishlistItemRequest request = new AddWishlistItemRequest(1L);
        
        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(request.productId())).thenReturn(testProduct);
        when(wishlistRepository.existsByUserAndProduct(testUser, testProduct)).thenReturn(false);

        // when
        wishlistService.addWishlistItem(email, request);

        // then
        ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(wishlistCaptor.capture());
        
        Wishlist savedWishlist = wishlistCaptor.getValue();
        assertThat(savedWishlist.getUser()).isEqualTo(testUser);
        assertThat(savedWishlist.getProduct()).isEqualTo(testProduct);
        
        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(request.productId());
        verify(wishlistRepository).existsByUserAndProduct(testUser, testProduct);
    }

    @Test
    @DisplayName("[addWishlistItem][실패] - 이미 존재하는 상품")
    void addWishlistItem_Fail_AlreadyExists() {
        // given
        String email = "test@example.com";
        AddWishlistItemRequest request = new AddWishlistItemRequest(1L);
        
        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(request.productId())).thenReturn(testProduct);
        when(wishlistRepository.existsByUserAndProduct(testUser, testProduct)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlistItem(email, request))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus());
        
        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(request.productId());
        verify(wishlistRepository).existsByUserAndProduct(testUser, testProduct);
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("[removeWishlistItem][성공] - 위시리스트 상품 삭제")
    void removeWishlistItem_Success() {
        // given
        String email = "test@example.com";
        Long productId = 1L;
        
        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(wishlistRepository.findByUserAndProduct(testUser, testProduct)).thenReturn(Optional.of(testWishlistItem));

        // when
        wishlistService.removeWishlistItem(email, productId);

        // then
        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(wishlistRepository).findByUserAndProduct(testUser, testProduct);
        verify(wishlistRepository).delete(testWishlistItem);
    }

    @Test
    @DisplayName("[removeWishlistItem][실패] - 존재하지 않는 상품")
    void removeWishlistItem_Fail_NotFound() {
        // given
        String email = "test@example.com";
        Long productId = 1L;
        
        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(wishlistRepository.findByUserAndProduct(testUser, testProduct)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.removeWishlistItem(email, productId))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());
        
        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(wishlistRepository).findByUserAndProduct(testUser, testProduct);
        verify(wishlistRepository, never()).delete(any());
    }
}