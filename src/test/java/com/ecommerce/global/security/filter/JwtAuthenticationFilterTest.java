package com.ecommerce.global.security.filter;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import com.ecommerce.global.infra.security.jwt.filter.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private String validAccessToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .address("서울시")
                .password("encoded_password")
                .role(UserRole.USER)
                .build();

        validAccessToken = "valid.access.token";

        // SecurityContext 초기화
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("[JWT 인증][성공] - 유효한 JWT 토큰으로 인증")
    void authenticateWithValidJwtToken() throws ServletException, IOException {
        // Given
        Cookie accessTokenCookie = new Cookie("access_token", validAccessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
        when(jwtProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(jwtProvider.getEmailFromToken(validAccessToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][통과] - JWT 토큰이 없는 경우 인증 없이 통과")
    void noJwtToken_ShouldPassWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getCookies()).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][실패] - 유효하지 않은 JWT 토큰으로 인증")
    void authenticateWithInvalidJwtToken() throws ServletException, IOException {
        // Given
        Cookie invalidTokenCookie = new Cookie("access_token", "invalid.token");
        when(request.getCookies()).thenReturn(new Cookie[]{invalidTokenCookie});
        when(jwtProvider.validateAccessToken("invalid.token")).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][실패] - Refresh Token을 Access Token으로 사용")
    void authenticateWithRefreshToken_ShouldFail() throws ServletException, IOException {
        // Given
        String refreshToken = "refresh.token.here";
        Cookie refreshTokenCookie = new Cookie("access_token", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(jwtProvider.validateAccessToken(refreshToken)).thenReturn(false); // 타입 불일치로 실패

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][실패] - 빈 문자열 토큰으로 인증")
    void authenticateWithEmptyToken() throws ServletException, IOException {
        // Given
        Cookie emptyTokenCookie = new Cookie("access_token", "");
        when(request.getCookies()).thenReturn(new Cookie[]{emptyTokenCookie});
        when(jwtProvider.validateAccessToken("")).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][실패] - null 토큰으로 인증")
    void authenticateWithNullToken() throws ServletException, IOException {
        // Given
        Cookie nullTokenCookie = new Cookie("access_token", null);
        when(request.getCookies()).thenReturn(new Cookie[]{nullTokenCookie});

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtProvider, never()).validateAccessToken(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][통과] - 다른 이름의 쿠키만 있는 경우 인증 없이 통과")
    void onlyOtherCookies_ShouldPassWithoutAuthentication() throws ServletException, IOException {
        // Given
        Cookie otherCookie = new Cookie("other_cookie", "some_value");
        Cookie sessionCookie = new Cookie("JSESSIONID", "session123");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie, sessionCookie});

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtProvider, never()).validateAccessToken(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][성공] - 여러 쿠키 중 access_token 쿠키 찾아서 인증")
    void findAccessTokenAmongMultipleCookies() throws ServletException, IOException {
        // Given
        Cookie sessionCookie = new Cookie("JSESSIONID", "session123");
        Cookie accessTokenCookie = new Cookie("access_token", validAccessToken);
        Cookie otherCookie = new Cookie("other_cookie", "other_value");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie, accessTokenCookie, otherCookie});
        when(jwtProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(jwtProvider.getEmailFromToken(validAccessToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtProvider).validateAccessToken(validAccessToken);
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("[JWT 인증][실패] - UserDetailsService에서 예외 발생")
    void userDetailsServiceException_ShouldFailAuthentication() throws ServletException, IOException {
        // Given
        Cookie accessTokenCookie = new Cookie("access_token", validAccessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
        when(jwtProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(jwtProvider.getEmailFromToken(validAccessToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenThrow(new RuntimeException("사용자를 찾을 수 없습니다"));

        // When - 필터가 예외를 잡아서 처리하므로 정상 실행됨
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then - 예외 발생으로 인증은 설정되지 않지만 필터는 계속 진행
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
} 