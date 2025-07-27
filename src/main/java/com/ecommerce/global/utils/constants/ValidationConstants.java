package com.ecommerce.global.utils.constants;

/**
 * 유효성 검증 관련 상수 정의
 */

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ValidationConstants {
    
    // 닉네임 제한
    public static final int NICKNAME_MIN_LENGTH = 2;
    public static final int NICKNAME_MAX_LENGTH = 30;
    
    // 비밀번호 제한
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;
    
    // 이메일 제한
    public static final int EMAIL_MAX_LENGTH = 100;
    
    // 전화번호 제한
    public static final int PHONE_NUMBER_MAX_LENGTH = 20;
    
    // 주소 제한
    public static final int ADDRESS_MAX_LENGTH = 200;
    
    // 역할 제한
    public static final int ROLE_MAX_LENGTH = 20;
    
    // 전화번호 패턴
    public static final String PHONE_NUMBER_PATTERN = "^01[0-9]-[0-9]{4}-[0-9]{4}$";
} 