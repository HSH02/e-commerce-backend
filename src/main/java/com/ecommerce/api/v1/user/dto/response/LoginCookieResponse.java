package com.ecommerce.api.v1.user.dto.response;

import org.springframework.http.ResponseCookie;

public record LoginCookieResponse(
    ResponseCookie accessCookie,
    ResponseCookie refreshCookie
) {} 