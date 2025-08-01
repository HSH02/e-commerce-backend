package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.wishlist.dto.request.AddWishlistItemRequest;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistItemDto;
import com.ecommerce.api.v1.wishlist.dto.response.WishlistResponseDto;
import com.ecommerce.domain.product.entity.Product;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.Wishlist;
import com.ecommerce.domain.user.repository.WishlistRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final ProductService productService;

    /**
     * 사용자의 위시리스트를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 위시리스트 응답 DTO
     */
    public WishlistResponseDto getWishlist(String email) {
        User user = userService.findByEmail(email);
        List<Wishlist> wishlistItems = wishlistRepository.findByUser(user);
        
        List<WishlistItemDto> items = wishlistItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return WishlistResponseDto.builder()
                .items(items)
                .totalItems(items.size())
                .build();
    }

    /**
     * 위시리스트에 상품을 추가합니다.
     *
     * @param email 사용자 이메일
     * @param request 위시리스트 상품 추가 요청 DTO
     * @throws ServiceException 이미 위시리스트에 존재하는 상품인 경우
     */
    @Transactional
    public void addWishlistItem(String email, AddWishlistItemRequest request) {
        User user = userService.findByEmail(email);
        Product product = productService.findProductEntityById(request.productId());
        
        // 이미 위시리스트에 존재하는지 확인
        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new ServiceException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        Wishlist wishlistItem = Wishlist.builder()
                .user(user)
                .product(product)
                .build();
        
        wishlistRepository.save(wishlistItem);
    }

    /**
     * 위시리스트에서 상품을 삭제합니다.
     *
     * @param email 사용자 이메일
     * @param productId 상품 ID
     * @throws ServiceException 위시리스트에 존재하지 않는 상품인 경우
     */
    @Transactional
    public void removeWishlistItem(String email, Long productId) {
        User user = userService.findByEmail(email);
        Product product = productService.findProductEntityById(productId);
        
        // 위시리스트에 존재하는지 확인
        Wishlist wishlistItem = wishlistRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
        
        wishlistRepository.delete(wishlistItem);
    }
    
    /**
     * Wishlist 엔티티를 WishlistItemDto로 변환합니다.
     *
     * @param wishlist Wishlist 엔티티
     * @return WishlistItemDto
     */
    private WishlistItemDto convertToDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        
        // 상품 이미지가 있는 경우 첫 번째 이미지를 사용
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0).getImageUrl();
        }
        
        return WishlistItemDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .imageUrl(imageUrl)
                .addedAt(wishlist.getCreatedAt())
                .build();
    }
}