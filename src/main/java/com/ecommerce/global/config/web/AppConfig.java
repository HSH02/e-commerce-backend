package com.ecommerce.global.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppConfig {
}
