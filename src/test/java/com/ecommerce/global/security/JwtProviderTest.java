package com.ecommerce.global.security;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static com.ecommerce.global.utils.constants.TokenType.ACCESS;
import static com.ecommerce.global.utils.constants.TokenType.REFRESH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();

        // 테스트용 설정 주입
        ReflectionTestUtils.setField(jwtProvider, "secret",
                "testSecretKeyForTestingPurposesOnlyMustBeLongEnoughForHmacSha256ASDBHJKASDBHASD");
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", "30m");
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpiration", "7d");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .phoneNumber("010-1234-5678")
                .address("서울시")
                .password("encoded_password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("[토큰 생성][성공] - Access Token 생성")
    void generateAndValidateAccessToken() {
        // Given & When
        String accessToken = jwtProvider.generateAccessToken(testUser);

        // Then
        assertThat(accessToken).isNotNull();
        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.validateAccessToken(accessToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(accessToken)).isEqualTo("test@example.com");
        assertThat(jwtProvider.getRoleFromToken(accessToken)).isEqualTo("USER");
        assertThat(jwtProvider.getTokenTypeEnum(accessToken)).isEqualTo(ACCESS);
    }

    @Test
    @DisplayName("[토큰 생성][성공] - Refresh Token 생성")
    void generateAndValidateRefreshToken() {
        // Given & When
        String refreshToken = jwtProvider.generateRefreshToken(testUser);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtProvider.validateRefreshToken(refreshToken)).isTrue();
        assertThat(jwtProvider.getEmailFromToken(refreshToken)).isEqualTo("test@example.com");
        assertThat(jwtProvider.getRoleFromToken(refreshToken)).isEqualTo("USER");
        assertThat(jwtProvider.getTokenTypeEnum(refreshToken)).isEqualTo(REFRESH);
    }

    @Test
    @DisplayName("[토큰 검증][실패] - Access Token을 Refresh Token 검증에 사용")
    void validateWrongTokenType_AccessTokenAsRefresh() {
        // Given
        String accessToken = jwtProvider.generateAccessToken(testUser);

        // When & Then
        assertThat(jwtProvider.validateAccessToken(accessToken)).isTrue();
        assertThat(jwtProvider.validateRefreshToken(accessToken)).isFalse(); // 타입 불일치
    }

    @Test
    @DisplayName("[토큰 검증][실패] - Refresh Token을 Access Token 검증에 사용")
    void validateWrongTokenType_RefreshTokenAsAccess() {
        // Given
        String refreshToken = jwtProvider.generateRefreshToken(testUser);

        // When & Then
        assertThat(jwtProvider.validateRefreshToken(refreshToken)).isTrue();
        assertThat(jwtProvider.validateAccessToken(refreshToken)).isFalse(); // 타입 불일치
    }

    @Test
    @DisplayName("[토큰 검증][실패] - 유효하지 않은 토큰 검증")
    void validateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThat(jwtProvider.validateToken(invalidToken)).isFalse();
        assertThat(jwtProvider.validateAccessToken(invalidToken)).isFalse();
        assertThat(jwtProvider.validateRefreshToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("[토큰 검증][실패] - null 토큰 검증")
    void validateNullToken() {
        // When & Then
        assertThat(jwtProvider.validateToken(null)).isFalse();
        assertThat(jwtProvider.validateAccessToken(null)).isFalse();
        assertThat(jwtProvider.validateRefreshToken(null)).isFalse();
    }

    @Test
    @DisplayName("[토큰 검증][실패] - 빈 문자열 토큰 검증")
    void validateEmptyToken() {
        // When & Then
        assertThat(jwtProvider.validateToken("")).isFalse();
        assertThat(jwtProvider.validateAccessToken("")).isFalse();
        assertThat(jwtProvider.validateRefreshToken("")).isFalse();
    }

    @Test
    @DisplayName("[토큰 검증][실패] - 잘못된 형식의 토큰 검증")
    void validateMalformedToken() {
        // Given
        String malformedToken = "not.a.jwt";

        // When & Then
        assertThat(jwtProvider.validateToken(malformedToken)).isFalse();
    }

    @Test
    @DisplayName("[만료시간 파싱][성공] - 분 단위 형식")
    void parseExpirationMinutes() {
        // Given
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", "30m");

        // When
        String token = jwtProvider.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("[만료시간 파싱][성공] - 시간 단위 형식")
    void parseExpirationHours() {
        // Given
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", "2h");

        // When
        String token = jwtProvider.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("[만료시간 파싱][성공] - 일 단위 형식")
    void parseExpirationDays() {
        // Given
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpiration", "7d");

        // When
        String token = jwtProvider.generateRefreshToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("[만료시간 파싱][예외 발생] - 잘못된 형식")
    void parseInvalidExpirationFormat() {
        // Given
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", "invalid");

        // When & Then
        assertThatThrownBy(() -> jwtProvider.generateAccessToken(testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 Duration 형식");
    }

    @Test
    @DisplayName("[만료시간 파싱][예외 발생] - 빈 문자열 형식")
    void parseEmptyExpiration() {
        // Given
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", "");

        // When & Then
        assertThatThrownBy(() -> jwtProvider.generateAccessToken(testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration이 설정되지 않았습니다");
    }

} 