package com.ecommerce.global.init;

import com.ecommerce.api.v1.product.dto.request.AddProductRequest;
import com.ecommerce.domain.product.service.ProductService;
import com.ecommerce.domain.user.entity.User;
import com.ecommerce.domain.user.entity.UserRole;
import com.ecommerce.domain.user.repository.UserRepository;
import com.ecommerce.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevProfileDataInitializer implements CommandLineRunner {

    private final ProductService productService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private final List<String> productNames = Arrays.asList(
            "프리미엄 헤드폰", "무선 마우스", "기계식 키보드",
            "울트라 HD 모니터", "게이밍 노트북", "스마트폰",
            "블루투스 스피커", "피트니스 트래커", "스마트 워치", "태블릿"
    );

    private final List<String> brands = Arrays.asList(
            "테크마스터", "일렉트로기어", "디지프로", "스마트테크",
            "퓨처테크", "이노베이트테크", "넥스트젠", "프라임테크"
    );

    private final List<String> categories = Arrays.asList(
            "전자제품", "컴퓨터", "오디오", "게이밍",
            "모바일", "액세서리", "웨어러블", "스마트홈"
    );

    private final List<String> descriptions = Arrays.asList(
            "프리미엄 기능을 갖춘 고품질 제품입니다.",
            "최적의 성능을 위한 최신 기술이 적용되었습니다.",
            "전문가와 애호가를 위해 설계되었습니다.",
            "스타일, 기능성, 내구성을 결합한 제품입니다.",
            "뛰어난 품질로 일상적인 사용에 완벽합니다.",
            "혁신적인 디자인과 최첨단 기능을 갖추고 있습니다.",
            "동급 최고의 성능과 신뢰성을 제공합니다.",
            "강력한 기능을 갖춘 세련된 디자인입니다."
    );

    @Override
    public void run(String... args) {
        log.info("테스트 상품 생성 중...");

        try {
            // 랜덤 상품 생성
            for (int i = 0; i < 10; i++) {
                createRandomProduct();
            }
            log.info("랜덤 상품 생성 완료.");

            // 테스트 유저 생성
            createTestUsers();
            log.info("테스트 유저 생성 완료.");
        } catch (Exception e) {
            log.error("개발 프로필 데이터 초기화 오류: {}", e.getMessage(), e);
        }
    }

    private void createTestUsers() {
        log.info("테스트 유저 초기화 중...");
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        // a@example.com부터 c@example.com까지 3개의 테스트 유저 생성
        for (char c = 'a'; c <= 'c'; c++) {
            String email = c + "@example.com";

            // 이미 존재하는 유저인지 확인
            if (userRepository.existsByEmail(email)) {
                log.info("이미 존재하는 유저입니다: {}", email);
                continue;
            }

            // 유저 생성
            User user = User.builder()
                    .email(email)
                    .nickname("테스트유저" + c)
                    .password(encodedPassword)
                    .phoneNumber("010-1234-567" + (c - 'a' + 1))
                    .address("서울시 강남구 테헤란로 " + (c - 'a' + 1) + "23")
                    .role(UserRole.USER)
                    .emailVerified(true)
                    .build();

            userRepository.save(user);
            log.info("테스트 유저 생성: {}", email);
        }
    }

    private void createRandomProduct() {
        String name = getRandomElement(productNames) + " " + random.nextInt(1000);
        String brand = getRandomElement(brands);
        String description = getRandomElement(descriptions);
        BigDecimal price = BigDecimal.valueOf(10 + random.nextInt(990));
        int stockQuantity = 10 + random.nextInt(90);

        List<String> productCategories = new ArrayList<>();
        int categoryCount = 1 + random.nextInt(3);
        for (int i = 0; i < categoryCount; i++) {
            String category = getRandomElement(categories);
            if (!productCategories.contains(category)) {
                productCategories.add(category);
            }
        }

        AddProductRequest request = new AddProductRequest(
                name,
                description,
                brand,
                price,
                stockQuantity,
                null,
                productCategories
        );

        productService.addProduct(request);
        log.info("랜덤 상품 생성: {} (카테고리: {})", name, productCategories);
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}