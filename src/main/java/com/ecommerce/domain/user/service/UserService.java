package com.ecommerce.domain.user.service;

import com.ecommerce.domain.user.dto.request.RegisterRequest;
import com.ecommerce.domain.user.dto.response.UserInfoResponse;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.repository.UserRepository;
import com.ecommerce.global.utils.exception.ErrorCode;
import com.ecommerce.global.utils.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("회원가입 시도: {}", request.email());

        validatePasswordConfirmation(request);
        validateEmailDuplication(request.email());
        validateNicknameDuplication(request.nickname());
        validatePhoneNumberDuplication(request.phoneNumber());

        User user = createUser(request);
        userRepository.save(user);

        log.info("회원가입 성공: userId={}, email={}", user.getId(), user.getEmail());
    }

    private User createUser(RegisterRequest request) {
        return User.builder()
                .address(request.address())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .nickname(request.nickname())
                .role(UserRole.USER)
                .password(passwordEncoder.encode(request.password()))
                .build();
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.EMAIL_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Authentication authentication) {
        String email = extractEmailFromAuthentication(authentication);
        String nickname = findByEmail(email).getNickname();
        return new UserInfoResponse(nickname);
    }

    private String extractEmailFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        throw new IllegalStateException("지원하지 않는 인증 주체 타입입니다: " + principal.getClass().getName());
    }

    private void validatePasswordConfirmation(RegisterRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new ServiceException(ErrorCode.PASSWORD_NOT_EQUAL);
        }
    }

    private void validateEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateNicknameDuplication(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new ServiceException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void validatePhoneNumberDuplication(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ServiceException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }
}
