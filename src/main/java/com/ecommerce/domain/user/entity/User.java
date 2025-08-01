package com.ecommerce.domain.user.entity;

import com.ecommerce.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

import static com.ecommerce.global.utils.constants.ValidationConstants.*;

@Entity
@SQLRestriction("is_deleted = false")
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
    private String nickname;

    @Column(length = PHONE_NUMBER_MAX_LENGTH)
    private String phoneNumber;

    @Column(length = ADDRESS_MAX_LENGTH)
    private String address;

    @Builder.Default
    @Column(nullable = false, length = ROLE_MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.GUEST;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Column(nullable = false, unique = true, length = EMAIL_MAX_LENGTH)
    private String email;

    @Column(length = PASSWORD_MAX_LENGTH)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccounts.add(socialAccount);
    }

}
