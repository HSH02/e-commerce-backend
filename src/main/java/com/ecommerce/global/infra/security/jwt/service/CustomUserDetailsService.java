package com.ecommerce.global.infra.security.jwt.service;

import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ecommerce.global.utils.constants.SecurityConstants.ROLE_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        try {
            User user = userService.findByEmail(email);
            boolean isDisabled = user.isDeleted();
            String password = user.getPassword() != null ? user.getPassword() : "";

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(password)
                    .authorities(List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name())))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(isDisabled)
                    .build();

        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + email, e);
        }
    }
}