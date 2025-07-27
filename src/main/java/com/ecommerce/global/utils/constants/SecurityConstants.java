package com.ecommerce.global.utils.constants;

import lombok.NoArgsConstructor;

/**
 * 보안 관련 상수 정의
 */
@NoArgsConstructor
public final class SecurityConstants {

    // JWT 클레임 키
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String ROLE_CLAIM = "role";
    
    // 쿠키 이름
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    
    // 쿠키 경로
    public static final String COOKIE_PATH = "/";
    
    // 권한 접두사
    public static final String ROLE_PREFIX = "ROLE_";
} 