package com.ecommerce.global.security.service;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.service.UserService;
import com.ecommerce.global.infra.security.jwt.service.CustomUserDetailsService;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User normalUser;
    private User deletedUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .nickname("user123")
                .phoneNumber("010-1234-5678")
                .address("서울시")
                .password("encoded_password")
                .role(UserRole.USER)
                .build();

        deletedUser = User.builder()
                .id(2L)
                .email("deleted@test.com")
                .nickname("deleted123")
                .phoneNumber("010-9999-9999")
                .address("서울시")
                .password("encoded_password")
                .role(UserRole.USER)
                .build();

        // isDeleted를 true로 설정 (리플렉션 사용)
        ReflectionTestUtils.setField(deletedUser, "isDeleted", true);

        adminUser = User.builder()
                .id(3L)
                .email("admin@test.com")
                .nickname("admin123")
                .phoneNumber("010-0000-0000")
                .address("서울시")
                .password("encoded_password")
                .role(UserRole.ADMIN)
                .build();
    }

    @Test
    @DisplayName("[사용자 로드][성공] - 일반 사용자 정보 로드")
    void loadUserByUsername_NormalUser_Success() {
        // Given
        when(userService.findByEmail("user@test.com")).thenReturn(normalUser);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@test.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded_password");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        // 권한 확인
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("[사용자 로드][성공] - 관리자 사용자 정보 로드")
    void loadUserByUsername_AdminUser_Success() {
        // Given
        when(userService.findByEmail("admin@test.com")).thenReturn(adminUser);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@test.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded_password");
        assertThat(userDetails.isEnabled()).isTrue();

        // 관리자 권한 확인
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("[사용자 로드][비활성화 상태] - 삭제된 사용자 정보 로드")
    void loadUserByUsername_DeletedUser_ShouldBeDisabled() {
        // Given
        when(userService.findByEmail("deleted@test.com")).thenReturn(deletedUser);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("deleted@test.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("deleted@test.com");
        assertThat(userDetails.isEnabled()).isFalse(); // 삭제된 사용자는 비활성화
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("[사용자 로드][예외 발생] - 존재하지 않는 사용자")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Given
        when(userService.findByEmail("nonexistent@test.com"))
                .thenThrow(new ServiceException(ErrorCode.EMAIL_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: nonexistent@test.com");
    }

    @Test
    @DisplayName("[사용자 로드][예외 변환] - UserService 예외 발생")
    void loadUserByUsername_ServiceException_ThrowsUsernameNotFoundException() {
        // Given
        when(userService.findByEmail("error@test.com"))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("error@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: error@test.com");
    }

    @Test
    @DisplayName("[사용자 로드][예외 발생] - null 이메일 입력")
    void loadUserByUsername_NullEmail() {
        // Given
        when(userService.findByEmail(null))
                .thenThrow(new ServiceException(ErrorCode.EMAIL_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("[사용자 로드][예외 발생] - 빈 문자열 이메일 입력")
    void loadUserByUsername_EmptyEmail() {
        // Given
        when(userService.findByEmail(""))
                .thenThrow(new ServiceException(ErrorCode.EMAIL_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("[권한 검증][성공] - ROLE 접두사 형식 확인")
    void checkAuthorityFormat() {
        // Given
        when(userService.findByEmail("user@test.com")).thenReturn(normalUser);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@test.com");

        // Then
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .allMatch(authority -> authority.startsWith("ROLE_"));
    }
}