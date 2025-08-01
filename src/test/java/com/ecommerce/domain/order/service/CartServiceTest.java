package com.ecommerce.domain.order.service;

import com.ecommerce.api.v1.cart.dto.request.AddCartItemRequest;
import com.ecommerce.api.v1.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.api.v1.cart.dto.response.CartItemDto;
import com.ecommerce.api.v1.cart.dto.response.CartResponseDto;
import com.ecommerce.domain.order.entity.Cart;
import com.ecommerce.domain.order.entity.CartItem;
import com.ecommerce.domain.order.repository.CartItemRepository;
import com.ecommerce.domain.order.repository.CartRepository;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.entity.ProductImage;
import com.ecommerce.domain.product.entity.ProductStatus;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.service.UserService;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
    private ProductImage testProductImage;

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

        testProduct.getImages().add(testProductImage);

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .cartItems(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();

        testCart.addItem(testCartItem);
    }

    @Test
    @DisplayName("[getCart][성공] - 사용자의 장바구니 조회")
    void getCart_Success() {
        // given
        String email = "test@example.com";

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // when
        CartResponseDto result = cartService.getCart(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);

        CartItemDto item = result.getItems().get(0);
        assertThat(item.getProductId()).isEqualTo(testProduct.getId());
        assertThat(item.getProductName()).isEqualTo(testProduct.getName());
        assertThat(item.getPrice()).isEqualTo(testProduct.getPrice());
        assertThat(item.getQuantity()).isEqualTo(testCartItem.getQuantity());
        assertThat(item.getImageUrl()).isEqualTo(testProductImage.getImageUrl());
        assertThat(item.getSubtotal()).isEqualTo(BigDecimal.valueOf(20000)); // 10000 * 2

        verify(userService).findByEmail(email);
        verify(cartRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("[getCart][성공] - 장바구니가 없는 경우 새로 생성")
    void getCart_CreateNewCart_Success() {
        // given
        String email = "test@example.com";

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(Cart.builder()
                .id(1L)
                .user(testUser)
                .cartItems(new ArrayList<>())
                .build());

        // when
        CartResponseDto result = cartService.getCart(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);

        verify(userService).findByEmail(email);
        verify(cartRepository).findByUser(testUser);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("[addCartItem][성공] - 새 상품 장바구니에 추가")
    void addCartItem_NewItem_Success() {
        // given
        String email = "test@example.com";
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(request.productId())).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());

        // when
        cartService.addCartItem(email, request);

        // then
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemCaptor.capture());

        CartItem savedCartItem = cartItemCaptor.getValue();
        assertThat(savedCartItem.getCart()).isEqualTo(testCart);
        assertThat(savedCartItem.getProduct()).isEqualTo(testProduct);
        assertThat(savedCartItem.getQuantity()).isEqualTo(request.quantity());

        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(request.productId());
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
    }

    @Test
    @DisplayName("[addCartItem][성공] - 기존 상품 수량 증가")
    void addCartItem_ExistingItem_Success() {
        // given
        String email = "test@example.com";
        AddCartItemRequest request = new AddCartItemRequest(1L, 3);
        int originalQuantity = testCartItem.getQuantity(); // 2

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(request.productId())).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));

        // when
        cartService.addCartItem(email, request);

        // then
        assertThat(testCartItem.getQuantity()).isEqualTo(originalQuantity + request.quantity()); // 2 + 3 = 5

        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(request.productId());
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
        verify(cartItemRepository, never()).save(any()); // Should not save a new item
    }

    @Test
    @DisplayName("[updateCartItem][성공] - 장바구니 상품 수량 수정")
    void updateCartItem_Success() {
        // given
        String email = "test@example.com";
        Long productId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));

        // when
        cartService.updateCartItem(email, productId, request);

        // then
        assertThat(testCartItem.getQuantity()).isEqualTo(request.quantity()); // Updated to 5

        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
    }

    @Test
    @DisplayName("[updateCartItem][실패] - 존재하지 않는 장바구니 상품")
    void updateCartItem_NotFound_Fail() {
        // given
        String email = "test@example.com";
        Long productId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.updateCartItem(email, productId, request))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());

        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
    }

    @Test
    @DisplayName("[removeCartItem][성공] - 장바구니에서 상품 삭제")
    void removeCartItem_Success() {
        // given
        String email = "test@example.com";
        Long productId = 1L;

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));

        // when
        cartService.removeCartItem(email, productId);

        // then
        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("[removeCartItem][실패] - 존재하지 않는 장바구니 상품")
    void removeCartItem_NotFound_Fail() {
        // given
        String email = "test@example.com";
        Long productId = 1L;

        when(userService.findByEmail(email)).thenReturn(testUser);
        when(productService.findProductEntityById(productId)).thenReturn(testProduct);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.removeCartItem(email, productId))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getHttpStatus());

        verify(userService).findByEmail(email);
        verify(productService).findProductEntityById(productId);
        verify(cartRepository).findByUser(testUser);
        verify(cartItemRepository).findByCartAndProduct(testCart, testProduct);
        verify(cartItemRepository, never()).delete(any());
    }
}