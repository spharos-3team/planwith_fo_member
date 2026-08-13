# planwith_fo_member

PlanWith FO Member 서비스 (`planwith-fo-member`).

| 항목 | 값 |
| --- | --- |
| Compose / Eureka 이름 | `planwith-fo-member` |
| 이미지 | `planwith/planwith-fo-member:latest` |
| 포트 | `8082` |
| API prefix | `/api/v1` |
| 배포 확인 | `GET /api/planwith-fo-member/deploy-check` |

## 로컬 실행

1. infra MySQL이 `127.0.0.1:3307`에서 떠 있어야 합니다 (`planwith-infra` compose).
2. `member_db`에 `terms.is_required` + 약관 시드가 적용돼 있어야 합니다.

```bash
export JAVA_HOME="/c/Users/G27/.jdks/ms-17.0.19"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew bootRun
```

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- 기본 PortOne은 `PORTONE_STUB_ENABLED=true` (실연동 시 storeId/channelKey/apiSecret 넣고 stub=false)

## API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/v1/auth/email-verifications` | 이메일 인증번호 발송 |
| POST | `/api/v1/auth/email-verifications/confirm` | 이메일 인증번호 확인 |
| POST | `/api/v1/auth/phone-verifications` | 본인인증 준비 (포트원 SDK 파라미터) |
| POST | `/api/v1/auth/phone-verifications/confirm` | 본인인증 완료 확인 |
| GET | `/api/v1/terms` | 약관 목록 |
| GET | `/api/v1/members/nicknames/availability` | 닉네임 중복확인 (2~10자) |
| POST | `/api/v1/members` | 로컬 회원가입 (201, 이메일+본인인증 필수) |
| POST | `/api/v1/auth/{provider}/signup` | 소셜 회원가입 (`google\|naver\|kakao`, 본인인증 필수) |
| POST | `/api/v1/auth/login` | 로컬 로그인 (Access body + Refresh HttpOnly Cookie) |
| POST | `/api/v1/auth/refresh` | 토큰 재발급 (`/auth/reissue` 별칭) |
| POST | `/api/v1/auth/logout` | 로그아웃 (204, Refresh Cookie 폐기) |
| GET | `/oauth2/jwks` | Access Token 검증용 공개키 |

### 로컬 로그인

```json
POST /api/v1/auth/login
{ "email": "user@example.com", "password": "Password1!" }
```

- 응답 body: `tokenType`, `accessToken`, `accessTokenExpiresIn`, `user.userId/roles/scopes`
- Refresh Token은 JSON에 포함하지 않음 → `Set-Cookie: refresh_token=...; HttpOnly; Path=/api/v1/auth`
- Access 검증은 Gateway 책임. Member는 발급·JWKS만 담당
- 로컬에서 RSA 키 경로 미설정 시 ephemeral 키 생성 (`JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH` 권장)

### 소셜 회원가입 (스텁)

1. 본인인증 confirm (스텁 id)
2. `POST /api/v1/auth/google/signup`

```json
{
  "authorizationCode": "stub:google-123:user@example.com",
  "nickname": "소셜닉",
  "phoneNumber": "01012345678",
  "agreements": [
    { "termUuid": "11111111-1111-1111-1111-111111111111", "agreed": true },
    { "termUuid": "22222222-2222-2222-2222-222222222222", "agreed": true }
  ]
}
```

실연동: `SOCIAL_STUB_ENABLED=false` + `GOOGLE_/KAKAO_/NAVER_CLIENT_*` 환경변수.

공통 응답: `ApiResponse`

### 본인인증 흐름 (포트원 KG이니시스)

1. `POST /api/v1/auth/phone-verifications` → `storeId`, `channelKey`, `identityVerificationId`
2. FE: `PortOne.requestIdentityVerification({ storeId, channelKey, identityVerificationId, bypass... })`
3. `POST /api/v1/auth/phone-verifications/confirm` with `identityVerificationId`
4. 회원가입 시 같은 `phoneNumber` 사용

스텁 모드 confirm 예: `identityVerificationId = "identity-verification-stub-01012345678"`

## 검증

```bash
./gradlew clean test
./gradlew clean build
```
