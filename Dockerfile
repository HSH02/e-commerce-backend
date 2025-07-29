# =============================================================================
# Stage 1: Builder - 애플리케이션 빌드 (운영용)
# =============================================================================
FROM gradle:8.7.0-jdk21-jammy as builder

WORKDIR /app

# 빌드 설정 파일 먼저 복사 (캐싱 최적화)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (소스 변경 시 재다운로드 방지)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew build --no-daemon -x test

# =============================================================================
# Stage 2: 개발용 - 볼륨 마운팅 활용
# =============================================================================
FROM gradle:8.7.0-jdk21-jammy as dev

WORKDIR /app

# Gradle Wrapper 설정
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# 빌드 파일만 복사 (소스코드는 볼륨으로 마운팅됨)
COPY build.gradle settings.gradle ./

# 의존성 미리 다운로드
RUN ./gradlew dependencies --no-daemon

# 포트 노출
EXPOSE 8080

# 개발용 실행 명령 (소스코드는 볼륨에서 가져옴)
CMD ["./gradlew", "bootRun", "--no-daemon"]

# =============================================================================
# Stage 3: 운영용 - 애플리케이션 실행
# =============================================================================
FROM openjdk:21-slim as prod

WORKDIR /app

# 보안을 위한 비root 사용자 생성
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]