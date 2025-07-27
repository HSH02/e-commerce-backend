package com.ecommerce.domain.user.service;

import com.ecommerce.domain.user.dto.response.TokenPair;
import com.ecommerce.global.infra.security.jwt.TokenCookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieResponseService {

    private final TokenCookieProvider tokenCookieProvider;

    public void addLoginCookies(HttpServletResponse response, TokenPair tokens) {
        ResponseCookie accessCookie = tokenCookieProvider.createAccessTokenCookie(tokens.accessToken());
        ResponseCookie refreshCookie = tokenCookieProvider.createRefreshTokenCookie(tokens.refreshToken());

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    public void addExpiredCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = tokenCookieProvider.createExpiredAccessTokenCookie();
        ResponseCookie refreshCookie = tokenCookieProvider.createExpiredRefreshTokenCookie();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}
