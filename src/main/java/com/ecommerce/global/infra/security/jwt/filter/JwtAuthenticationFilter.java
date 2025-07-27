package com.ecommerce.global.infra.security.jwt.filter;

import com.ecommerce.global.infra.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.ecommerce.global.utils.constants.SecurityConstants.ACCESS_TOKEN_COOKIE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        // trace-id
        String trace_id = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", trace_id);


        // access token
        String accessToken = getAccessTokenFromCookie(request);

        if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
            try {
                String email = jwtProvider.getEmailFromToken(accessToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.warn("JWT 인증 중 사용자 정보 로드 실패: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);

    }

    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }


}
