### 프로젝트 패키지 구조 다이어그램

```
com.ecommerce
├── ECommerceApplication.java
├── domain
│   └── user
│       ├── controller
│       ├── dto
│       ├── entity
│       ├── repository
│       └── service
└── global
    ├── config
    ├── constants
    ├── dto
    ├── exception
    ├── redis
    ├── security
    │   ├── jwt
    │   └── oauth
    └── utils
```

1. **domain**: 비즈니스 도메인 관련 코드를 포함합니다.
    - **user**: 사용자 관련 기능을 담당하는 패키지로, MVC 패턴에 따라 구성되어 있습니다.
        - controller: API 엔드포인트 정의
        - dto: 데이터 전송 객체
        - entity: 데이터베이스 모델
        - repository: 데이터 액세스 계층
        - service: 비즈니스 로직

2. **global**: 애플리케이션 전반에 걸쳐 사용되는 공통 기능을 포함합니다.
    - **config**: 애플리케이션 설정
    - **constants**: 상수 정의
    - **dto**: 공통 데이터 전송 객체
    - **exception**: 예외 처리 관련 클래스
    - **redis**: Redis 관련 기능
    - **security**: 보안 관련 기능
        - jwt: JWT 인증 관련 클래스
        - oauth: OAuth 인증 관련 클래스
    - **utils**: 유틸리티 클래스

