package com.ecommerce.global.infra.security.jwt;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.global.utils.constants.TokenType;
import com.ecommerce.global.utils.DurationUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.ecommerce.global.utils.constants.SecurityConstants.ROLE_CLAIM;
import static com.ecommerce.global.utils.constants.SecurityConstants.TOKEN_TYPE_CLAIM;
import static com.ecommerce.global.utils.constants.TokenType.ACCESS;
import static com.ecommerce.global.utils.constants.TokenType.REFRESH;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private String accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private String refreshTokenExpiration;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration, ACCESS);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration, REFRESH);
    }

    private String generateToken(User user, String expiration, TokenType tokenType) {
        Date now = new Date();
        long expirationMillis = DurationUtils.parseToMillis(expiration);
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(user.getEmail())
                        .claim(ROLE_CLAIM, user.getRole().name())
                        .claim(TOKEN_TYPE_CLAIM, tokenType.getValue())  // 토큰 타입 추가
                        .issuedAt(now)
                        .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(ROLE_CLAIM, String.class);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(TOKEN_TYPE_CLAIM, String.class);
    }

    public boolean validateAccessToken(String token) {
        if (!validateToken(token) && isTokenExpired(token)) {
            return false;
        }

        try {
            TokenType tokenType = getTokenTypeEnum(token);
            return tokenType.isAccessToken();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid token type in token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        if (!validateToken(token) && isTokenExpired(token)) {
            return false;
        }

        try {
            TokenType tokenType = getTokenTypeEnum(token);
            return tokenType.isRefreshToken();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid token type in token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("토큰 만료 확인 실패: {}", e.getMessage());
            return true;
        }
    }

    public TokenType getTokenTypeEnum(String token) {
        String tokenTypeValue = getTokenType(token);
        return TokenType.fromValue(tokenTypeValue);
    }
}
