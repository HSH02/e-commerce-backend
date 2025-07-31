package com.ecommerce.global.config.security;

import com.ecommerce.global.infra.security.jwt.filter.JwtAuthenticationFilter;
import com.ecommerce.global.infra.security.oauth.CustomOAuth2UserService;
import com.ecommerce.global.infra.security.oauth.OAuth2AuthenticationFailureHandler;
import com.ecommerce.global.infra.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .cors(
                        cors -> cors.configurationSource(corsConfigurationSource())
                )

                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 세선 정책
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // JWT 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 헤더 정책
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                // OAuth2
                .oauth2Login(oauth2 -> oauth2
                        // 로그인 성공 후 사용자 정보를 가져올 때의 설정
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )

                        // 로그인 성공/실패 시 처리할 핸들러 지정
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )


                .authorizeHttpRequests(requests ->
                        requests

                                // OAuth2
                                .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()


                                // 공개 API
                                .requestMatchers("/api/v1/users/register").permitAll()      // 회원가입
                                .requestMatchers("/api/v1/users/login").permitAll()         // 로그인

                                // 개발/문서화 관련
                                .requestMatchers("/h2-console/**").permitAll()              // H2 콘솔
                                .requestMatchers("/swagger-ui/**").permitAll()              // Swagger UI
                                .requestMatchers("/v3/api-docs/**").permitAll()             // OpenAPI 문서
                                .requestMatchers("/swagger-resources/**").permitAll()       // Swagger 리소스
                                .requestMatchers("/api/v1/redis-test/**").permitAll()       // Redis

                                // 그 외 모든 요청은 인증 필요

                                // 개발용
                                .anyRequest().permitAll()

//                                 .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin
        // TODO : 개발용임
        configuration.setAllowedOriginPatterns(List.of("*"));

//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:8080",
//                "https://localhost:8080",
//                "http://localhost:3000",
//                "https://localhost:3000"
//        ));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));

        // 자격 증명 허용 설정
        configuration.setAllowCredentials(true);

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠기 헤더 노출 설정
        configuration.setExposedHeaders(Arrays.asList(
                "Set-Cookie",
                "Authorization",
                "Access-Control-Allow-Headers",
                "Access-Control-Allow-Origin"
        ));


        // preflight 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        // CORS 설정을 특정 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
