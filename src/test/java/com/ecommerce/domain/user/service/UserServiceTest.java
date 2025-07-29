package com.ecommerce.domain.user.service;

import com.ecommerce.api.v1.user.dto.request.RegisterRequest;
import com.ecommerce.api.v1.user.dto.response.UserInfoResponse;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.repository.UserRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
                "hong123", "010-1234-5678", "서울시 강남구",
                "hong@test.com", "password123", "password123"
        );

        mockUser = User.builder()
                .id(1L)
                .nickname("hong123")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@test.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("[회원가입][성공] - 유효한 정보로 회원가입")
    void register_Success() {
        // Given
        given(userRepository.existsByEmail(validRegisterRequest.email())).willReturn(false);
        given(userRepository.existsByNickname(validRegisterRequest.nickname())).willReturn(false);
        given(userRepository.existsByPhoneNumber(validRegisterRequest.phoneNumber())).willReturn(false);
        given(passwordEncoder.encode(validRegisterRequest.password())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        // When
        userService.register(validRegisterRequest);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(validRegisterRequest.email());
        assertThat(savedUser.getNickname()).isEqualTo(validRegisterRequest.nickname());
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("[회원가입][실패] - 비밀번호 확인 불일치")
    void register_Fail_PasswordNotEqual() {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest(
                "hong123", "010-1234-5678", "서울시 강남구",
                "hong@test.com", "password123", "differentPassword"
        );

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.register(invalidRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.PASSWORD_NOT_EQUAL.getHttpStatus());
        assertThat(exception.getMessage()).contains("비밀번호가 일치하지 않습니다");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("[회원가입][실패] - 이메일 중복")
    void register_Fail_DuplicateEmail() {
        // Given
        given(userRepository.existsByEmail(validRegisterRequest.email())).willReturn(true);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.register(validRegisterRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL.getHttpStatus());
        assertThat(exception.getMessage()).contains("이미 사용중인 이메일입니다");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("[회원가입][실패] - 닉네임 중복")
    void register_Fail_DuplicateNickname() {
        // Given
        given(userRepository.existsByEmail(validRegisterRequest.email())).willReturn(false);
        given(userRepository.existsByNickname(validRegisterRequest.nickname())).willReturn(true);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.register(validRegisterRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME.getHttpStatus());
        assertThat(exception.getMessage()).contains("이미 사용중인 이름입니다");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("[회원가입][실패] - 전화번호 중복")
    void register_Fail_DuplicatePhoneNumber() {
        // Given
        given(userRepository.existsByEmail(validRegisterRequest.email())).willReturn(false);
        given(userRepository.existsByNickname(validRegisterRequest.nickname())).willReturn(false);
        given(userRepository.existsByPhoneNumber(validRegisterRequest.phoneNumber())).willReturn(true);

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.register(validRegisterRequest));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.DUPLICATE_PHONE_NUMBER.getHttpStatus());
        assertThat(exception.getMessage()).contains("이미 사용중인 전화번호입니다");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("[이메일로 사용자 찾기][성공] - 존재하는 이메일")
    void findByEmail_Success() {
        // Given
        String email = "hong@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));

        // When
        User foundUser = userService.findByEmail(email);

        // Then
        assertThat(foundUser).isEqualTo(mockUser);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("[이메일로 사용자 찾기][실패] - 존재하지 않는 이메일")
    void findByEmail_Fail_EmailNotFound() {
        // Given
        String email = "notfound@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> userService.findByEmail(email));

        assertThat(exception.getCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND.getHttpStatus());
        assertThat(exception.getMessage()).contains("해당 이메일을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("[내 정보 조회][성공] - UserDetails 인증")
    void getMyInfo_Success_UserDetails() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn("hong@test.com");
        given(userRepository.findByEmail("hong@test.com")).willReturn(Optional.of(mockUser));

        // When
        UserInfoResponse response = userService.getMyInfo(authentication);

        // Then
        assertThat(response.name()).isEqualTo(mockUser.getNickname());
    }

    @Test
    @DisplayName("[내 정보 조회][실패] - 지원하지 않는 인증 주체")
    void getMyInfo_Fail_UnsupportedPrincipal() {
        // Given
        given(authentication.getPrincipal()).willReturn("unsupportedPrincipal");

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.getMyInfo(authentication));

        assertThat(exception.getMessage()).contains("지원하지 않는 인증 주체 타입입니다");
    }
}