package com.ecommerce.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductStatus {
    ACTIVE("판매중"),
    INACTIVE("판매중지"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("단종"),
    PENDING("승인대기");

    private final String description;

    public String getDescription() {
        return description;
    }
}