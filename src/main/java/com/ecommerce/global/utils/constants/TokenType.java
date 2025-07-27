package com.ecommerce.global.utils.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public enum TokenType {
    ACCESS("ACCESS"),
    REFRESH("REFRESH");

    private final String value;

    public boolean isAccessToken() {
        return this == ACCESS;
    }

    public boolean isRefreshToken() {
        return this == REFRESH;
    }

    /**
     * 문자열 값으로부터 TokenType을 찾아 반환합니다.
     */
    public static TokenType fromValue(String value) {
        for (TokenType type : TokenType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 토큰 타입 값: " + value);
    }
}