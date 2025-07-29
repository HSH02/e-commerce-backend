package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.user.dto.response.TokenPair;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.infra.redis.RefreshTokenService;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenManagementServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TokenManagementService tokenManagementService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("hong@test.com")
                .password("encodedPassword")
                .nickname("hong123")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("[토큰 생성][성공] - 사용자 정보로 토큰 쌍 생성")
    void generateTokens_Success() {
        // Given
        String accessToken = "generated.access.token";
        String refreshToken = "generated.refresh.token";

        given(jwtProvider.generateAccessToken(mockUser)).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(mockUser)).willReturn(refreshToken);
        doNothing().when(refreshTokenService).saveRefreshToken(mockUser.getEmail(), refreshToken);

        // When
        TokenPair result = tokenManagementService.generateTokens(mockUser);

        // Then
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        verify(jwtProvider, times(1)).generateAccessToken(mockUser);
        verify(jwtProvider, times(1)).generateRefreshToken(mockUser);
        verify(refreshTokenService, times(1)).saveRefreshToken(mockUser.getEmail(), refreshToken);
    }

    @Test
    @DisplayName("[토큰 갱신][성공] - 유효한 Refresh Token으로 갱신")
    void refreshTokens_Success() {
        // Given
        String validRefreshToken = "valid.refresh.token";
        String email = "hong@test.com";
        String newAccessToken = "new.access.token";
        String newRefreshToken = "new.refresh.token";

        given(jwtProvider.validateRefreshToken(validRefreshToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(validRefreshToken)).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(Optional.of(validRefreshToken));
        given(userService.findByEmail(email)).willReturn(mockUser);
        given(jwtProvider.generateAccessToken(mockUser)).willReturn(newAccessToken);
        given(jwtProvider.generateRefreshToken(mockUser)).willReturn(newRefreshToken);
        doNothing().when(refreshTokenService).saveRefreshToken(email, newRefreshToken);

        // When
        TokenPair result = tokenManagementService.refreshTokens(validRefreshToken);

        // Then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);

        verify(jwtProvider, times(1)).validateRefreshToken(validRefreshToken);
        verify(jwtProvider, times(1)).getEmailFromToken(validRefreshToken);
        verify(refreshTokenService, times(1)).getRefreshToken(email);
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("[토큰 갱신][실패] - 유효하지 않은 Refresh Token")
    void refreshTokens_Fail_InvalidRefreshToken() {
        // Given
        String invalidRefreshToken = "invalid.refresh.token";
        given(jwtProvider.validateRefreshToken(invalidRefreshToken)).willReturn(false);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenManagementService.refreshTokens(invalidRefreshToken));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.getHttpStatus());
        assertThat(exception.getMessage()).contains("유효하지 않은 Refresh Token 입니다");

        verify(jwtProvider, never()).getEmailFromToken(anyString());
        verify(refreshTokenService, never()).getRefreshToken(anyString());
    }

    @Test
    @DisplayName("[토큰 갱신][실패] - Redis에 저장된 토큰과 불일치")
    void refreshTokens_Fail_TokenMismatch() {
        // Given
        String clientRefreshToken = "client.refresh.token";
        String storedRefreshToken = "stored.refresh.token";
        String email = "hong@test.com";

        given(jwtProvider.validateRefreshToken(clientRefreshToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(clientRefreshToken)).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(Optional.of(storedRefreshToken));
        doNothing().when(refreshTokenService).deleteRefreshToken(email);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenManagementService.refreshTokens(clientRefreshToken));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_REFRESH_REDIS_FAILED.getHttpStatus());
        assertThat(exception.getMessage()).contains("Refresh Token이 유효하지 않거나 탈취되었을 수 있습니다");

        verify(refreshTokenService, times(1)).deleteRefreshToken(email);
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("[토큰 갱신][실패] - Redis에 토큰이 존재하지 않음")
    void refreshTokens_Fail_TokenNotFoundInRedis() {
        // Given
        String refreshToken = "refresh.token";
        String email = "hong@test.com";

        given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
        given(jwtProvider.getEmailFromToken(refreshToken)).willReturn(email);
        given(refreshTokenService.getRefreshToken(email)).willReturn(Optional.empty());
        doNothing().when(refreshTokenService).deleteRefreshToken(email);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> tokenManagementService.refreshTokens(refreshToken));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_REFRESH_REDIS_FAILED.getHttpStatus());
        assertThat(exception.getMessage()).contains("Refresh Token이 유효하지 않거나 탈취되었을 수 있습니다");

        verify(refreshTokenService, times(1)).deleteRefreshToken(email);
    }

    @Test
    @DisplayName("[토큰 무효화][성공] - 사용자 이메일로 Refresh Token 삭제")
    void revokeRefreshToken_Success() {
        // Given
        String email = "hong@test.com";
        doNothing().when(refreshTokenService).deleteRefreshToken(email);

        // When
        tokenManagementService.revokeRefreshToken(email);

        // Then
        verify(refreshTokenService, times(1)).deleteRefreshToken(email);
    }
}