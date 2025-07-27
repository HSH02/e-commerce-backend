package com.ecommerce.global.security.service;

import com.ecommerce.global.infra.security.jwt.TokenCookieProvider;
import com.ecommerce.global.utils.DurationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static com.ecommerce.global.utils.constants.SecurityConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

class TokenCookieProviderTest {

    private TokenCookieProvider tokenCookieProvider;
    private final String accessTokenMaxAge = "10m";
    private final String refreshTokenMaxAge = "1d";
    private final boolean secureFlag = true;
    private final boolean httpOnlyFlag = true;
    private final String sameSitePolicy = "Lax";

    @BeforeEach
    void setUp() {
        tokenCookieProvider = new TokenCookieProvider();

        // private 필드 값 강제 설정
        ReflectionTestUtils.setField(tokenCookieProvider, "accessTokenMaxAge", accessTokenMaxAge);
        ReflectionTestUtils.setField(tokenCookieProvider, "refreshTokenMaxAge", refreshTokenMaxAge);
        ReflectionTestUtils.setField(tokenCookieProvider, "SECURE_FLAG", secureFlag);
        ReflectionTestUtils.setField(tokenCookieProvider, "HTTP_ONLY_FLAG", httpOnlyFlag);
        ReflectionTestUtils.setField(tokenCookieProvider, "SAME_SITE_POLICY", sameSitePolicy);
    }

    @Test
    @DisplayName("[쿠키 생성][성공] - Access Token 쿠키 생성")
    void createAccessTokenCookie_Success() {
        // Given
        String accessToken = "test.access.token";
        Duration expectedMaxAge = DurationUtils.parse(accessTokenMaxAge);

        // When
        ResponseCookie cookie = tokenCookieProvider.createAccessTokenCookie(accessToken);

        // Then
        assertThat(cookie.getName()).isEqualTo(ACCESS_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(accessToken);
        assertThat(cookie.getPath()).isEqualTo(COOKIE_PATH);
        assertThat(cookie.getMaxAge()).isEqualTo(expectedMaxAge);
        assertThat(cookie.isHttpOnly()).isEqualTo(httpOnlyFlag);
        assertThat(cookie.isSecure()).isEqualTo(secureFlag);
        assertThat(cookie.getSameSite()).isEqualTo(sameSitePolicy);
    }

    @Test
    @DisplayName("[쿠키 생성][성공] - Refresh Token 쿠키 생성")
    void createRefreshTokenCookie_Success() {
        // Given
        String refreshToken = "test.refresh.token";
        Duration expectedMaxAge = DurationUtils.parse(refreshTokenMaxAge);

        // When
        ResponseCookie cookie = tokenCookieProvider.createRefreshTokenCookie(refreshToken);

        // Then
        assertThat(cookie.getName()).isEqualTo(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.getPath()).isEqualTo(COOKIE_PATH);
        assertThat(cookie.getMaxAge()).isEqualTo(expectedMaxAge);
        assertThat(cookie.isHttpOnly()).isEqualTo(httpOnlyFlag);
        assertThat(cookie.isSecure()).isEqualTo(secureFlag);
        assertThat(cookie.getSameSite()).isEqualTo(sameSitePolicy);
    }

    @Test
    @DisplayName("[쿠키 생성][성공] - 만료된 Access Token 쿠키 생성")
    void createExpiredAccessTokenCookie_Success() {
        // When
        ResponseCookie cookie = tokenCookieProvider.createExpiredAccessTokenCookie();

        // Then
        assertThat(cookie.getName()).isEqualTo(ACCESS_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.getPath()).isEqualTo(COOKIE_PATH);
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        assertThat(cookie.isHttpOnly()).isEqualTo(httpOnlyFlag);
        assertThat(cookie.isSecure()).isEqualTo(secureFlag);
        assertThat(cookie.getSameSite()).isEqualTo(sameSitePolicy);
    }

    @Test
    @DisplayName("[쿠키 생성][성공] - 만료된 Refresh Token 쿠키 생성")
    void createExpiredRefreshTokenCookie_Success() {
        // When
        ResponseCookie cookie = tokenCookieProvider.createExpiredRefreshTokenCookie();

        // Then
        assertThat(cookie.getName()).isEqualTo(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.getPath()).isEqualTo(COOKIE_PATH);
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        assertThat(cookie.isHttpOnly()).isEqualTo(httpOnlyFlag);
        assertThat(cookie.isSecure()).isEqualTo(secureFlag);
        assertThat(cookie.getSameSite()).isEqualTo(sameSitePolicy);
    }
}
