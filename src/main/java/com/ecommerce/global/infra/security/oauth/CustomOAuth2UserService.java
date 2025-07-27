package com.ecommerce.global.infra.security.oauth;

import com.ecommerce.domain.user.entity.SocialAccount;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.repository.SocialAccountRepository;
import com.ecommerce.domain.user.repository.UserRepository;
import com.ecommerce.global.utils.constants.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        // Spring Security의 Principal 객체로 사용될 DefaultOAuth2User를 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName())),
                attributes.attributes(),
                attributes.nameAttributeKey());
    }

    @Transactional
    public User saveOrUpdate(OAuthAttributes attributes) {
        String providerId = extractProviderId(attributes).toString();

        // 1. 기존 소셜 계정 확인
        Optional<SocialAccount> existingSocialAccount = socialAccountRepository.findByProviderAndProviderId(attributes.provider(), providerId);
        if (existingSocialAccount.isPresent()) {
            return existingSocialAccount.get().getUser();
        }

        // 2. 이메일로 기존 사용자 확인 및 처리
        User user = findOrCreateUser(attributes);

        // 3. 소셜 계정 연결
        linkSocialAccount(user, attributes.provider(), providerId);

        return user;
    }

    private static Object extractProviderId(OAuthAttributes attributes) {
        Map<String, Object> userAttributes = attributes.attributes();
        String nameAttributeKey = attributes.nameAttributeKey();
        Object providerIdObject = userAttributes.get(nameAttributeKey);

        if (providerIdObject == null) {
            log.error("로그인 실패: OAuth Provider로부터 고유 ID를 받지 못했습니다. Attributes: {}", userAttributes);
            OAuth2Error error = new OAuth2Error(
                    "missing_provider_id",
                    "OAuth2 provider의 응답에 고유 ID(" + nameAttributeKey + ")가 없습니다.",
                    null
            );
            throw new OAuth2AuthenticationException(error);
        }
        return providerIdObject;
    }

    private User findOrCreateUser(OAuthAttributes attributes) {
        Optional<User> existingUser = userRepository.findByEmail(attributes.email());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.info("기존 계정({})에 소셜 계정({})을 연결합니다.", user.getEmail(), attributes.provider());
            return user;
        } else {
            User newUser = attributes.toEntity();
            userRepository.save(newUser);
            log.info("신규 소셜 계정 유저({})를 생성합니다.", newUser.getEmail());
            return newUser;
        }
    }

    private void linkSocialAccount(User user, Provider provider, String providerId) {
        SocialAccount newSocialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .build();
        user.addSocialAccount(newSocialAccount);
    }
}

