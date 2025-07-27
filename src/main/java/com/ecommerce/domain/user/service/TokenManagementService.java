package com.ecommerce.domain.user.service;

import com.ecommerce.domain.user.dto.response.TokenPair;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.global.infra.redis.RefreshTokenService;
import com.ecommerce.global.infra.security.jwt.JwtProvider;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenManagementService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public TokenPair generateTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair refreshTokens(String refreshToken) {
        validateRefreshToken(refreshToken);

        String email = jwtProvider.getEmailFromToken(refreshToken);
        validateStoredRefreshToken(email, refreshToken);

        User user = userService.findByEmail(email);
        return generateTokens(user);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token 입니다.");
            throw new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void validateStoredRefreshToken(String email, String clientToken) {
        refreshTokenService.getRefreshToken(email)
                .filter(storedToken -> storedToken.equals(clientToken))
                .orElseThrow(() -> {
                    log.warn("Refresh Token이 유효하지 않거나 탈취되었을 가능성이 있습니다. email: {}", email);
                    refreshTokenService.deleteRefreshToken(email);
                    return new ServiceException(ErrorCode.INVALID_REFRESH_REDIS_FAILED);
                });
    }

    public void revokeRefreshToken(String email) {
        refreshTokenService.deleteRefreshToken(email);
        log.info("Redis에서 Refresh Token을 삭제 이메일={}", email);
    }

}
