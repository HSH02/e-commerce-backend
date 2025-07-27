package com.ecommerce.global.infra.security.jwt;

import com.ecommerce.global.utils.DurationUtils;
import com.ecommerce.global.utils.constants.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TokenCookieProvider {

    private static final Duration EXPIRED_MAX_AGE = Duration.ZERO;
    
    @Value("${jwt.access-token-expiration}")
    private String accessTokenMaxAge;

    @Value("${jwt.refresh-token-expiration}")
    private String refreshTokenMaxAge;

    @Value("${app.cookie.secure:false}")
    private boolean SECURE_FLAG;

    @Value("${app.cookie.http-only:true}")
    private boolean HTTP_ONLY_FLAG;

    @Value("${app.cookie.same-site:Strict}")
    private String SAME_SITE_POLICY;


    /**
     * Access Token용 쿠키 생성
     */
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .path(SecurityConstants.COOKIE_PATH)
                .maxAge(DurationUtils.parse(accessTokenMaxAge))
                .httpOnly(HTTP_ONLY_FLAG)
                .secure(SECURE_FLAG)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }

    /**
     * Refresh Token용 쿠키 생성
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path(SecurityConstants.COOKIE_PATH)
                .maxAge(DurationUtils.parse(refreshTokenMaxAge))
                .httpOnly(HTTP_ONLY_FLAG)
                .secure(SECURE_FLAG)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }

    /**
     * 만료된 Access Token 쿠키 생성 (로그아웃용)
     */
    public ResponseCookie createExpiredAccessTokenCookie() {
        return ResponseCookie.from(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, "")
                .path(SecurityConstants.COOKIE_PATH)
                .maxAge(EXPIRED_MAX_AGE)
                .httpOnly(HTTP_ONLY_FLAG)
                .secure(SECURE_FLAG)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }

    /**
     * 만료된 Refresh Token 쿠키 생성 (로그아웃용)
     */
    public ResponseCookie createExpiredRefreshTokenCookie() {
        return ResponseCookie.from(SecurityConstants.REFRESH_TOKEN_COOKIE_NAME, "")
                .path(SecurityConstants.COOKIE_PATH)
                .maxAge(EXPIRED_MAX_AGE)
                .httpOnly(HTTP_ONLY_FLAG)
                .secure(SECURE_FLAG)
                .sameSite(SAME_SITE_POLICY)
                .build();
    }


}
