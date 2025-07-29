package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.user.dto.request.LoginRequest;
import com.ecommerce.api.v1.user.dto.response.TokenPair;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenManagementService tokenManagementService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private User mockUser;
    private TokenPair mockTokenPair;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest("hong@test.com", "password123");

        mockUser = User.builder()
                .id(1L)
                .email("hong@test.com")
                .password("encodedPassword")
                .nickname("hong123")
                .role(UserRole.USER)
                .build();

        mockTokenPair = new TokenPair("access.token.jwt", "refresh.token.jwt");
    }

    @Test
    @DisplayName("[로그인][성공] - 유효한 사용자 인증")
    void authenticate_Success() {
        // Given
        given(userService.findByEmail(validLoginRequest.email())).willReturn(mockUser);
        given(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).willReturn(true);
        given(tokenManagementService.generateTokens(mockUser)).willReturn(mockTokenPair);

        // When
        TokenPair result = authService.authenticate(validLoginRequest);

        // Then
        assertThat(result).isEqualTo(mockTokenPair);
        assertThat(result.accessToken()).isEqualTo("access.token.jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh.token.jwt");

        verify(userService, times(1)).findByEmail(validLoginRequest.email());
        verify(passwordEncoder, times(1)).matches(validLoginRequest.password(), mockUser.getPassword());
        verify(tokenManagementService, times(1)).generateTokens(mockUser);
    }

    @Test
    @DisplayName("[로그인][실패] - 존재하지 않는 이메일")
    void authenticate_Fail_EmailNotFound() {
        // Given
        given(userService.findByEmail(validLoginRequest.email())).willThrow(new ServiceException(ErrorCode.EMAIL_NOT_FOUND));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.authenticate(validLoginRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND.getHttpStatus());
        assertThat(exception.getMessage()).contains("해당 이메일을 찾을 수 없습니다");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenManagementService, never()).generateTokens(any());
    }

    @Test
    @DisplayName("[로그인][실패] - 잘못된 비밀번호")
    void authenticate_Fail_WrongPassword() {
        // Given
        given(userService.findByEmail(validLoginRequest.email())).willReturn(mockUser);
        given(passwordEncoder.matches(validLoginRequest.password(), mockUser.getPassword())).willReturn(false);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.authenticate(validLoginRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.LOGIN_FAILED.getHttpStatus());
        assertThat(exception.getMessage()).contains("이메일 또는 비밀번호가 올바르지 않습니다");

        verify(tokenManagementService, never()).generateTokens(any());
    }

    @Test
    @DisplayName("[토큰 갱신][성공] - 유효한 Refresh Token")
    void refreshTokens_Success() {
        // Given
        String refreshToken = "valid.refresh.token";
        given(tokenManagementService.refreshTokens(refreshToken)).willReturn(mockTokenPair);

        // When
        TokenPair result = authService.refreshTokens(refreshToken);

        // Then
        assertThat(result).isEqualTo(mockTokenPair);
        verify(tokenManagementService, times(1)).refreshTokens(refreshToken);
    }

    @Test
    @DisplayName("[토큰 갱신][실패] - 유효하지 않은 Refresh Token")
    void refreshTokens_Fail_InvalidToken() {
        // Given
        String invalidRefreshToken = "invalid.refresh.token";
        given(tokenManagementService.refreshTokens(invalidRefreshToken))
                .willThrow(new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> authService.refreshTokens(invalidRefreshToken));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.getHttpStatus());
        assertThat(exception.getMessage()).contains("유효하지 않은 Refresh Token 입니다");
    }

    @Test
    @DisplayName("[로그아웃][성공] - 사용자 이메일로 토큰 무효화")
    void logout_Success() {
        // Given
        String email = "hong@test.com";
        doNothing().when(tokenManagementService).revokeRefreshToken(email);

        // When
        authService.logout(email);

        // Then
        verify(tokenManagementService, times(1)).revokeRefreshToken(email);
    }
}