package com.ecommerce.domain.user.entity;


public enum UserRole {
    USER(),
    ADMIN(),
    GUEST()
    ;

    public String getName() {
        return this.name();
    }
}
