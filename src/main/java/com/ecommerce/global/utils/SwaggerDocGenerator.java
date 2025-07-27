package com.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class SwaggerDocGenerator implements ApplicationListener<ApplicationReadyEvent> {

    private final RestTemplate restTemplate;

    public SwaggerDocGenerator() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
        String apiUrl = "http://localhost:" + port + "/v3/api-docs";
        Path outputPath = Paths.get("swagger.json");

        log.info("Swagger API 문서를 다음 경로에서 가져옵니다: {}", apiUrl);

        try {
            // API 문서 요청
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (response.getBody() != null) {
                // 파일에 저장
                Files.writeString(outputPath, response.getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Swagger API 문서가 성공적으로 'swagger.json' 파일로 저장되었습니다. 위치: {}", outputPath.toAbsolutePath());
            } else {
                log.warn("API 문서 응답이 비어있습니다.");
            }

        } catch (IOException e) {
            log.error("파일 저장 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("API 문서를 가져오는 중 오류가 발생했습니다.", e);
        }
    }
}