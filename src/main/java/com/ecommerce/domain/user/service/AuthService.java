package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.user.dto.request.LoginRequest;
import com.ecommerce.api.v1.user.dto.response.TokenPair;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenManagementService tokenManagementService;

    @Transactional
    public TokenPair authenticate(LoginRequest request) {
        User user = validateUserCredentials(request);
        TokenPair tokens = tokenManagementService.generateTokens(user);

        log.debug("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());
        return tokens;
    }

    @Transactional
    public TokenPair refreshTokens(String refreshToken) {
        TokenPair tokens = tokenManagementService.refreshTokens(refreshToken);
        log.info("토큰 갱신 성공");
        return tokens;
    }

    public void logout(String email) {
        tokenManagementService.revokeRefreshToken(email);
    }

    private User validateUserCredentials(LoginRequest request) {
        User user = userService.findByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("로그인 실패 - 잘못된 비밀번호: {}", request.email());
            throw new ServiceException(ErrorCode.LOGIN_FAILED);
        }

        return user;
    }
}
