## 기술 스택

- **Backend**: Spring Boot (Java 21)
- **캐싱**: Redis
- **컨테이너화**: Docker, Docker Compose
- **인증**: JWT
- **소셜 로그인**: Google OAuth2

##  시작하기

### 사전 요구사항

- [Docker](https://www.docker.com/get-started) 설치
- [Docker Compose](https://docs.docker.com/compose/install/) 설치
- JDK 21 (로컬 개발 시)
- Gradle (로컬 개발 시)

### 환경 변수 설정

1. 프로젝트 루트에 `.env` 파일을 생성하고 다음 변수들을 설정:

```env
# JWT 설정
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# Google OAuth2 설정
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### 애플리케이션 실행 방법

프로젝트는 **개발 환경**과 **운영 환경**을 위한 두 가지 Docker 설정을 제공합니다.

#### 개발 환경 (Dev)

개발 환경은 **실시간 코드 변경**을 지원하여 빠르게 개발할 수 있습니다.

1. 개발 환경으로 시작:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

2. 로그 확인:

```bash
docker-compose -f docker-compose.dev.yml logs -f app
```

3. **코드 변경 시 자동 적용!** - 코드를 수정하면 몇 초 내에 자동으로 반영됩니다

4. 개발 환경 중지:

```bash
docker-compose -f docker-compose.dev.yml down
```

#### 운영 환경 (Prod)

운영 환경은 **최적화된 성능**을 제공하는 프로덕션 설정입니다.

1. 운영 환경으로 시작:

```bash
docker-compose up -d
```

2. 로그 확인:

```bash
docker-compose logs -f
```

3. 운영 환경 중지:

```bash
docker-compose down
```

#### 💡 환경 간 주요 차이점

| 기능 | 개발 환경 | 운영 환경 |
|------|----------|----------|
| **코드 변경** | ✅ 실시간 적용 | ❌ 재빌드 필요 |
| **Spring DevTools** | ✅ 활성화 | ❌ 비활성화 |
| **이미지 크기** | 큼 (Gradle 포함) | 작음 (JRE만) |
| **시작 속도** | 느림 (컴파일) | 빠름 (JAR 실행) |
| **메모리 사용량** | 높음 | 낮음 |
| **로그 레벨** | DEBUG | INFO |

#### 로컬에서 직접 실행 (Docker 없이)

1. 애플리케이션 빌드:

```bash
./gradlew clean build
```

2. 애플리케이션 실행:

```bash
java -jar build/libs/*.jar
```

> **참고**: 로컬 실행 시 Redis 등의 의존성 서비스를 별도로 실행해야 합니다.

