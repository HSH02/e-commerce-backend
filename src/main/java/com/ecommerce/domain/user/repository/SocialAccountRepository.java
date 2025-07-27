package com.ecommerce.domain.user.repository;

import com.ecommerce.domain.user.entity.SocialAccount;
import com.ecommerce.global.utils.constants.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);
}