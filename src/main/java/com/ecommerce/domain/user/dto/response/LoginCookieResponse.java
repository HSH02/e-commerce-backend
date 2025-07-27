package com.ecommerce.domain.user.dto.response;

import org.springframework.http.ResponseCookie;

public record LoginCookieResponse(
    ResponseCookie accessCookie,
    ResponseCookie refreshCookie
) {} 