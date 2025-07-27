package com.ecommerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ECommerceApplicationTest {


    @Test
    @DisplayName("[애플리케이션][성공] - 정상 실행")
    void contextLoads() {
    }

}