package com.ecommerce.global.security.oauth;

import com.ecommerce.global.infra.security.oauth.OAuthAttributes;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.global.utils.constants.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthAttributesTest {

    @Test
    @DisplayName("[OAuth 사용자 속성][성공] - Google 정보로 OAuthAttributes 객체 생성")
    void of_Google_Success() {
        // Given
        String registrationId = "google";
        String userNameAttributeName = "sub";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("given_name", "홍길동");
        attributes.put("email", "test@example.com");

        // When
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributes);

        // Then
        assertThat(oAuthAttributes.attributes()).isEqualTo(attributes);
        assertThat(oAuthAttributes.nameAttributeKey()).isEqualTo(userNameAttributeName);
        assertThat(oAuthAttributes.nickname()).isEqualTo("홍길동");
        assertThat(oAuthAttributes.email()).isEqualTo("test@example.com");
        assertThat(oAuthAttributes.provider()).isEqualTo(Provider.GOOGLE);
    }

    @Test
    @DisplayName("[OAuth 사용자 속성][성공] - OAuthAttributes로부터 User 엔티티 생성")
    void toEntity_Success() {
        // Given
        String nickname = "홍길동";
        String email = "test@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        OAuthAttributes oAuthAttributes = OAuthAttributes.builder()
                .nickname(nickname)
                .email(email)
                .provider(Provider.GOOGLE)
                .attributes(attributes)
                .nameAttributeKey("sub")
                .build();

        // When
        User user = oAuthAttributes.toEntity();

        // Then
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("[Oauth 사용자 속성 빌더][성공] - Builder 패턴을 통한 객체 생성")
    void builder_Success() {
        // Given
        String nickname = "홍길동";
        String email = "test@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        String nameAttributeKey = "sub";

        // When
        OAuthAttributes oAuthAttributes = OAuthAttributes.builder()
                .nickname(nickname)
                .email(email)
                .provider(Provider.GOOGLE)
                .attributes(attributes)
                .nameAttributeKey(nameAttributeKey)
                .build();

        // Then
        assertThat(oAuthAttributes.nickname()).isEqualTo(nickname);
        assertThat(oAuthAttributes.email()).isEqualTo(email);
        assertThat(oAuthAttributes.provider()).isEqualTo(Provider.GOOGLE);
        assertThat(oAuthAttributes.attributes()).isEqualTo(attributes);
        assertThat(oAuthAttributes.nameAttributeKey()).isEqualTo(nameAttributeKey);
    }
}
