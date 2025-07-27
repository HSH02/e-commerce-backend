package com.ecommerce.global.infra.security.oauth;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import com.ecommerce.global.infra.redis.RefreshTokenService;
import com.ecommerce.global.infra.security.jwt.TokenCookieProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final TokenCookieProvider tokenCookieProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // DB에서 사용자 정보 조회
        User user = userService.findByEmail(email);

        // JWT 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // Redis Refresh Token 저장
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        // 쿠키 생성
        ResponseCookie accessTokenCookie = tokenCookieProvider.createAccessTokenCookie(accessToken);
        ResponseCookie refreshTokenCookie = tokenCookieProvider.createRefreshTokenCookie(refreshToken);

        // 응답 헤더에 쿠키 추가
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // 프론트엔드 URL로 리디렉션
        String targetUrl = createRedirectUrl(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String createRedirectUrl(HttpServletRequest request) {
        // 성공 후 리디렉션될 프론트엔드 페이지
        // 예: http://localhost:3000/
        return UriComponentsBuilder.fromUriString("http://localhost:3000")
                .build().toUriString();
    }
}