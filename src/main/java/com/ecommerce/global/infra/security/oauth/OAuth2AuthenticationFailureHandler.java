package com.ecommerce.global.infra.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 로그인 실패 : {}", exception.getMessage(), exception);

        String targetUrl = UriComponentsBuilder.fromUriString("/login")
                .queryParam("error", "true")
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}