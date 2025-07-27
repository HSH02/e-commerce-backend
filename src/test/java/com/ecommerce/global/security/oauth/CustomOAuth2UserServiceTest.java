package com.ecommerce.global.security.oauth;

import com.ecommerce.global.infra.security.oauth.CustomOAuth2UserService;
import com.ecommerce.global.infra.security.oauth.OAuthAttributes;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.repository.SocialAccountRepository;
import com.ecommerce.domain.user.repository.UserRepository;
import com.ecommerce.global.utils.constants.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ecommerce.global.utils.constants.Provider.GOOGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private User user;
    private OAuthAttributes attributes;

    @BeforeEach
    void setUp() {
        // 테스트용 User 엔티티 설정
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester") // 초기 닉네임
                .role(UserRole.USER)
                .build();

        // OAuth2 응답 속성 설정
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("id", "123456789");
        userAttributes.put("email", "test@example.com");
        userAttributes.put("name", "New Test User"); // OAuth에서 받아온 이름

        // OAuthAttributes 객체 설정 (업데이트될 닉네임)
        attributes = OAuthAttributes.builder()
                .attributes(userAttributes)
                .nameAttributeKey("id")
                .nickname("New Nickname") // OAuth에서 받아온 닉네임
                .email("test@example.com")
                .provider(GOOGLE)
                .build();
    }

    @Test
    @DisplayName("[OAuth2 로그인][성공] - 신규 사용자일 경우, 회원가입")
    void saveOrUpdate_whenNewUser_thenCreatesUserAndSocialAccount() {
        // given
        given(socialAccountRepository.findByProviderAndProviderId(any(Provider.class), anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        User resultUser = customOAuth2UserService.saveOrUpdate(attributes);

        // then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(resultUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(attributes.email());
        assertThat(savedUser.getNickname()).isEqualTo(attributes.nickname());
        assertThat(savedUser.getSocialAccounts()).hasSize(1);
        assertThat(savedUser.getSocialAccounts().getFirst().getProvider()).isEqualTo(GOOGLE);
        assertThat(savedUser.getSocialAccounts().getFirst().getProviderId()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("[OAuth2 로그인][성공] - 기존 사용자의 신규 소셜 연동일 경우, Provider 추가")
    void saveOrUpdate_whenExistingUserNewSocial_thenLinksSocialAccountAndUpdateNickname() {
        // given
        given(socialAccountRepository.findByProviderAndProviderId(any(Provider.class), anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        // when
        User resultUser = customOAuth2UserService.saveOrUpdate(attributes);

        // then
        // User는 새로 저장되지 않고, SocialAccount는 User의 socialAccounts에 추가되어 영속성 전이(cascade)로 저장됨
        verify(userRepository, never()).save(any(User.class));
        assertThat(resultUser.getId()).isEqualTo(user.getId());
        assertThat(resultUser.getSocialAccounts()).hasSize(1); // 소셜 계정 추가 확인
        assertThat(resultUser.getSocialAccounts().getFirst().getProvider()).isEqualTo(GOOGLE);
    }

    @Test
    @DisplayName("[OAuth2 로그인][실패] - Provider ID가 없을 경우, 예외를 발생시킨다")
    void saveOrUpdate_whenProviderIdIsNull_thenThrowsException() {
        // given
        Map<String, Object> malformedAttributes = new HashMap<>(attributes.attributes());
        malformedAttributes.remove("id"); // Provider ID 제거
        OAuthAttributes badAttributes = OAuthAttributes.of("google", "id", malformedAttributes);

        // when and then
        assertThatThrownBy(() -> customOAuth2UserService.saveOrUpdate(badAttributes))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("OAuth2 provider의 응답에 고유 ID(id)가 없습니다.");
    }
}