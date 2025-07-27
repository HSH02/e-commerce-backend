package com.ecommerce.global.infra.security.oauth;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.utils.constants.Provider;
import lombok.Builder;

import java.util.Map;

public record OAuthAttributes(
        Map<String, Object> attributes,
        String nameAttributeKey,
        String nickname,
        String email,
        Provider provider
) {

    @Builder
    public OAuthAttributes {
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다. Provider: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nickname((String) attributes.get("given_name"))
                .email((String) attributes.get("email"))
                .provider(Provider.GOOGLE)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .role(UserRole.USER)
                .build();
    }
}