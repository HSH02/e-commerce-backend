package com.ecommerce.api.v1.user.controller;

import com.ecommerce.api.v1.user.dto.request.LoginRequest;
import com.ecommerce.api.v1.user.dto.request.RegisterRequest;
import com.ecommerce.api.v1.user.dto.response.TokenPair;
import com.ecommerce.api.v1.user.dto.response.UserInfoResponse;
import com.ecommerce.domain.user.service.AuthService;
import com.ecommerce.global.infra.web.CookieResponseService;
import com.ecommerce.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private CookieResponseService cookieResponseService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        validRegisterRequest = new RegisterRequest(
                "hong123", "010-1234-5678", "서울시 강남구",
                "hong@test.com", "password123", "password123"
        );

        validLoginRequest = new LoginRequest("hong@test.com", "password123");
    }

    @Test
    @DisplayName("[회원가입][성공] - 유효한 정보로 회원가입")
    void register_Success() throws Exception {
        // Given
        doNothing().when(userService).register(any(RegisterRequest.class));
        String jsonRequest = objectMapper.writeValueAsString(validRegisterRequest);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("[로그인][성공] - 유효한 정보로 로그인")
    void login_Success() throws Exception {
        // Given
        TokenPair tokenPair = new TokenPair("access.token.test", "refresh.token.test");
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(tokenPair);
        doNothing().when(cookieResponseService).addLoginCookies(any(), any(TokenPair.class));

        String jsonRequest = objectMapper.writeValueAsString(validLoginRequest);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        // Then
        resultActions

                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
        verify(cookieResponseService, times(1)).addLoginCookies(any(), any(TokenPair.class));
    }

    @Test
    @DisplayName("[토큰 재발급][성공] - 유효한 Refresh Token으로 재발급")
    void refreshTokens_Success() throws Exception {
        // Given
        String validRefreshToken = "valid.refresh.token";
        TokenPair tokenPair = new TokenPair("new.access.token", "new.refresh.token");

        when(authService.refreshTokens(validRefreshToken)).thenReturn(tokenPair);
        doNothing().when(cookieResponseService).addLoginCookies(any(), any(TokenPair.class));

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/refresh")
                .cookie(new Cookie("refresh_token", validRefreshToken)));

        // Then
        resultActions

                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).refreshTokens(validRefreshToken);
        verify(cookieResponseService, times(1)).addLoginCookies(any(), any(TokenPair.class));
    }

    @Test
    @DisplayName("[토큰 재발급][실패] - 빈 토큰")
    void refreshTokens_Fail_BlankToken() throws Exception {
        // Given
        String blankToken = "";

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/refresh")
                .cookie(new Cookie("refresh_token", blankToken)));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("refresh token이 필요합니다"));

        // 빈 토큰이므로 서비스 호출이 발생하지 않아야 함
        verify(authService, never()).refreshTokens(any());
        verify(cookieResponseService, never()).addLoginCookies(any(), any());
    }

    @Test
    @DisplayName("[로그아웃][성공] - 인증된 사용자 로그아웃")
    void logout_Success_AuthenticatedUser() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("hong@test.com");
        doNothing().when(authService).logout("hong@test.com");
        doNothing().when(cookieResponseService).addExpiredCookies(any());

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/logout")
                .principal(authentication));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).logout("hong@test.com");
        verify(cookieResponseService, times(1)).addExpiredCookies(any());
    }

    @Test
    @DisplayName("[로그아웃][성공] - 인증되지 않은 사용자 로그아웃")
    void logout_Success_NotAuthenticatedUser() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        doNothing().when(cookieResponseService).addExpiredCookies(any());

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/logout")
                .principal(authentication));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());

        // 인증되지 않았으므로 서비스 호출이 발생하지 않아야 함
        verify(authService, never()).logout(any());
        verify(cookieResponseService, times(1)).addExpiredCookies(any());
    }

    @Test
    @DisplayName("[로그아웃][성공] - Authentication이 null인 경우")
    void logout_Success_NullAuthentication() throws Exception {
        // Given
        doNothing().when(cookieResponseService).addExpiredCookies(any());

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/users/logout"));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다"))
                .andExpect(jsonPath("$.timestamp").exists());

        // Authentication이 null이므로 서비스 호출이 발생하지 않아야 함
        verify(authService, never()).logout(any());
        verify(cookieResponseService, times(1)).addExpiredCookies(any());
    }

    @Test
    @DisplayName("[내 정보 조회][성공] - 인증된 사용자")
    void getMyInfo_Success() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userService.getMyInfo(authentication)).thenReturn(new UserInfoResponse("홍길동"));

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/me")
                .principal(authentication));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).getMyInfo(authentication);
    }

    @Test
    @DisplayName("[내 정보 조회][실패] - 인증되지 않은 사용자")
    void getMyInfo_Fail_NotAuthenticated() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/me")
                .principal(authentication));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다"));

        // 인증되지 않았으므로 서비스 호출이 발생하지 않아야 함
        verify(userService, never()).getMyInfo(any());
    }

    @Test
    @DisplayName("[내 정보 조회][실패] - Authentication이 null인 경우")
    void getMyInfo_Fail_NullAuthentication() throws Exception {
        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/users/me"));

        // Then
        resultActions
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다"));

        // Authentication이 null이므로 서비스 호출이 발생하지 않아야 함
        verify(userService, never()).getMyInfo(any());
    }

}