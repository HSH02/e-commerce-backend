package com.ecommerce.api.v1.user.controller;

import com.ecommerce.api.v1.user.dto.request.LoginRequest;
import com.ecommerce.api.v1.user.dto.request.RegisterRequest;
import com.ecommerce.api.v1.user.dto.response.UserInfoResponse;
import com.ecommerce.global.utils.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "유저")
public interface UserApiSpecification {

    @Operation(summary = "회원 가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created")
    })
    RsData<Void> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    RsData<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response);

    @Operation(summary = "토큰 갱신")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    RsData<Void> refreshTokens(@CookieValue("refresh_token") String refreshToken, HttpServletResponse response);

    @Operation(summary = "로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    RsData<Void> logout(Authentication authentication, HttpServletResponse response);

    @Operation(summary = "내 정보")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    RsData<UserInfoResponse> getMyInfo(Authentication authentication);
}