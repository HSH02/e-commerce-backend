package com.ecommerce.global.infra.redis;

import com.ecommerce.global.utils.DurationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token-expiration}")
    private String refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken) {
        Duration expirationDuration = DurationUtils.parse(refreshTokenExpiration);
        redisTemplate.opsForValue().set(email, refreshToken, expirationDuration);
        log.info("Redis에 Refresh Token 저장 완료: 이메일={}, 만료 기간={}", email, expirationDuration);

    }


    public Optional<String> getRefreshToken(String email) {
        String refreshToken =redisTemplate.opsForValue().get(email);
        log.debug("Redis에서 Refresh Token 조회: 이메일={}, 토큰={}", email, refreshToken != null ? "존재" : "없음");
        return Optional.ofNullable(refreshToken);
    }

    public void deleteRefreshToken(String email) {
        // Redis에서 키를 삭제합니다.
        Boolean deleted = redisTemplate.delete(email);
        if (deleted) {
            log.info("Redis에서 Refresh Token 삭제 완료: 이메일={}", email);
        } else {
            log.warn("Redis에서 Refresh Token 삭제 실패 또는 이미 존재하지 않음: 이메일={}", email);
        }
    }

}
