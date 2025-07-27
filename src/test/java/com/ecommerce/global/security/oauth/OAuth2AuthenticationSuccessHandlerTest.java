package com.ecommerce.global.security.oauth;

import com.ecommerce.global.infra.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import com.ecommerce.global.infra.redis.RefreshTokenService;
import com.ecommerce.global.infra.security.jwt.TokenCookieProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenCookieProvider tokenCookieProvider;

    @Mock
    private RefreshTokenService refreshTokenService; // NPE 방지를 위해 Mock 객체 선언

    @Mock
    private UserService userService;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication authentication;
    private OAuth2User oAuth2User;
    private User user;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authentication = mock(Authentication.class);
        oAuth2User = mock(OAuth2User.class);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .role(UserRole.USER)
                .build();

        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("[OAuth 인증성공][성공] - 인증 성공 후 토큰 생성, Redis 저장 및 리다이렉션")
    void onAuthenticationSuccess_Success() throws IOException {
        // Given
        String testAccessToken = "test.access.token";
        String testRefreshToken = "test.refresh.token";

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(jwtProvider.generateAccessToken(user)).thenReturn(testAccessToken);
        when(jwtProvider.generateRefreshToken(user)).thenReturn(testRefreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", testAccessToken).path("/").build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", testRefreshToken).path("/").build();
        when(tokenCookieProvider.createAccessTokenCookie(testAccessToken)).thenReturn(accessTokenCookie);
        when(tokenCookieProvider.createRefreshTokenCookie(testRefreshToken)).thenReturn(refreshTokenCookie);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        // 1. 사용자 정보 조회 검증
        verify(userService).findByEmail("test@example.com");

        // 2. 토큰 생성 검증
        verify(jwtProvider).generateAccessToken(user);
        verify(jwtProvider).generateRefreshToken(user);

        verify(refreshTokenService).saveRefreshToken(user.getEmail(), testRefreshToken);

        // 4. 쿠키 생성 검증
        verify(tokenCookieProvider).createAccessTokenCookie(testAccessToken);
        verify(tokenCookieProvider).createRefreshTokenCookie(testRefreshToken);

        // 5. 응답 헤더에 쿠키 추가 검증
        List<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        assertThat(setCookieHeaders).contains(accessTokenCookie.toString(), refreshTokenCookie.toString());

        // 6. 리다이렉션 검증
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), anyString());
    }
}