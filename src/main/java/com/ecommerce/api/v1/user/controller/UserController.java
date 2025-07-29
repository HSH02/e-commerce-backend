package com.ecommerce.api.v1.user.controller;

import com.ecommerce.api.v1.user.dto.request.LoginRequest;
import com.ecommerce.api.v1.user.dto.request.RegisterRequest;
import com.ecommerce.api.v1.user.dto.response.TokenPair;
import com.ecommerce.api.v1.user.dto.response.UserInfoResponse;
import com.ecommerce.domain.user.service.AuthService;
import com.ecommerce.global.infra.web.CookieResponseService;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.utils.dto.RsData;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApiSpecification {

    private final UserService userService;
    private final AuthService authService;
    private final CookieResponseService cookieResponseService;

    @PostMapping("/register")
    public RsData<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        userService.register(request);
        return RsData.success(HttpStatus.CREATED, null, "회원가입이 완료되었습니다");
    }

    @PostMapping("/login")
    public RsData<Void> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokens = authService.authenticate(request);
        cookieResponseService.addLoginCookies(response, tokens);

        return RsData.success(HttpStatus.OK, null, "로그인 성공");
    }

    @PostMapping("/refresh")
    public RsData<Void> refreshTokens(
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || StringUtils.isBlank(refreshToken)) {
            return RsData.error(HttpStatus.BAD_REQUEST, "refresh token이 필요합니다");
        }

        TokenPair tokens = authService.refreshTokens(refreshToken);
        cookieResponseService.addLoginCookies(response, tokens);

        return RsData.success(HttpStatus.OK, null, "토큰이 갱신되었습니다");
    }

    @PostMapping("/logout")
    public RsData<Void> logout(
            Authentication authentication,
            HttpServletResponse response
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            authService.logout(authentication.getName());
        }

        cookieResponseService.addExpiredCookies(response);
        return RsData.success(HttpStatus.OK, null, "로그아웃되었습니다");
    }

    @GetMapping("/me")
    public RsData<UserInfoResponse> getMyInfo(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return RsData.error(HttpStatus.FORBIDDEN, "접근 권한이 없습니다");
        }

        UserInfoResponse response = userService.getMyInfo(authentication);
        return RsData.success(HttpStatus.OK, response, "사용자 정보 조회 성공");
    }
}
