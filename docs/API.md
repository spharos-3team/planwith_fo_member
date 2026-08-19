# Member API 진행 현황

> 이슈 완료마다 이 문서를 갱신한다.  
> Swagger (로컬 bootRun): `http://localhost:8082/swagger-ui/index.html`  
> Swagger (다른 PC / Docker): `http://{SERVER_IP}:8000/swagger-ui.html` → `planwith-fo-member`  
> 공통 응답: `ApiResponse<T>`  
> 호출 경로: `Frontend → Gateway(:8000) → Member(:8082)` (Access 검증은 Gateway)

최종 갱신: 2026-08-19 (#27 회원 팔로우)

---

## 요약

| 상태 | 이슈 | 내용 |
| --- | --- | --- |
| ✅ Done | #1 | 로컬 회원가입 (이메일 인증·약관) |
| ✅ Done | #3 | 포트원 본인인증 |
| ✅ Done | #5 | 닉네임 중복확인 |
| ✅ Done | #6 | 소셜 회원가입 (Google/Naver/Kakao) |
| ✅ Done | #7 | 로컬 로그인·토큰 재발급·로그아웃·JWKS |
| ✅ Done | #8 | 소셜 로그인 |
| ✅ Done | #9 | 아이디 찾기·비밀번호 재설정 |
| ✅ Done | #10 | 내 회원정보·프로필·약관 동의 |
| ✅ Done | #17 | 본인인증 실명(`name`) DB·응답 반영 |
| ✅ Done | #25 | 본인인증 스텁 휴대폰·실명 지정 / 이메일 SMTP 실발송 |
| ✅ Done | #27 | 회원 팔로우 / 언팔로우 / 목록 / 상태 조회 |

---

## 완료된 API

| Issue | Method | Endpoint | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| #1 | POST | `/api/v1/auth/email-verifications` | X | 이메일 인증번호 발송 |
| #1 | POST | `/api/v1/auth/email-verifications/confirm` | X | 이메일 인증번호 확인 |
| #1 | GET | `/api/v1/terms` | X | 약관 목록 |
| #1 | POST | `/api/v1/members` | X | 로컬 회원가입 (201) |
| #3 | POST | `/api/v1/auth/phone-verifications` | X | 본인인증 준비 (포트원 SDK) |
| #3/#17 | POST | `/api/v1/auth/phone-verifications/confirm` | X | 본인인증 완료 확인 (`phoneNumber` + `name`) |
| #5 | GET | `/api/v1/members/nicknames/availability` | X | 닉네임 중복확인 (2~10자) |
| #6 | POST | `/api/v1/auth/{provider}/signup` | X | 소셜 회원가입 (비번 없음, 본인인증·닉네임·약관, 직후 토큰) |
| #7 | POST | `/api/v1/auth/login` | X | 로컬 로그인 |
| #7 | POST | `/api/v1/auth/refresh` | Refresh Cookie | 토큰 재발급 (`/auth/reissue` 별칭) |
| #7 | POST | `/api/v1/auth/logout` | Refresh Cookie | 로그아웃 (204) |
| #7 | GET | `/oauth2/jwks` | X | Access Token 공개키 |
| #8 | POST | `/api/v1/auth/{provider}/login` | X | 소셜 원클릭 로그인 (`isNewMember`) |
| #9 | POST | `/api/v1/auth/find-email` | X | 아이디 찾기 (본인인증 휴대폰) |
| #9 | POST | `/api/v1/auth/password/reset-requests` | X | 비밀번호 재설정 코드 발송 (로컬만) |
| #9 | POST | `/api/v1/auth/password/reset` | X | 비밀번호 재설정 (204, 로컬만) |
| #10 | GET | `/api/v1/members/me` | O | 내 회원정보 조회 (`name` 포함) |
| #10 | PATCH | `/api/v1/members/me` | O | **마이페이지 저장** (휴대폰·닉네임·선택약관·비번) |
| #10 | PATCH | `/api/v1/members/me/profile` | O | 위와 동일 (별칭) |
| #10 | DELETE | `/api/v1/members/me` | O | 회원 탈퇴 (soft → `DELETED`) |
| #10 | GET | `/api/v1/members/me/profile` | O | 내 프로필 조회 |
| #10 | POST | `/api/v1/members/me/profile/image` | O | 프로필 이미지 업로드 (stub URL) |
| #10 | GET | `/api/v1/terms/{termUuid}` | X | 약관 상세 |
| #10 | GET | `/api/v1/members/me/agreements` | O | 내 약관 동의 조회 (화면 로드용) |
| #10 | POST | `/api/v1/members/me/agreements` | O | 선택 약관만 단독 변경 (보조) |
| #10 | PATCH | `/api/v1/members/me/password` | O | 비밀번호만 단독 변경 (보조) |
| #10 | GET | `/api/v1/members/{memberUuid}/profile` | X | 공개 프로필 (`memberUuid`, 팔로우 수, 로그인 시 `isFollowing`) |
| #10 | GET | `/api/v1/members/{memberUuid}` | 내부 | 최소 공개정보 (Gateway Trust, `name` 포함) |
| #27 | GET | `/api/v1/members/search` | X | 회원 목록 (`memberUuid` 포함, `nickname` 검색, `page`/`size`) |
| #27 | POST | `/api/v1/members/{memberUuid}/follow` | O | 해당 회원 팔로우 (`followUuid`, `isActive`) |
| #27 | DELETE | `/api/v1/members/{memberUuid}/follow` | O | 언팔로우 (비활성화, 204) |
| #27 | GET | `/api/v1/members/{memberUuid}/followers` | X | 팔로워 요약 프로필 목록 (`page`, `size`) |
| #27 | GET | `/api/v1/members/{memberUuid}/followings` | X | 팔로잉 요약 프로필 목록 (`page`, `size`) |
| #27 | GET | `/api/v1/members/{memberUuid}/follow-status` | O | 내가 해당 회원을 팔로우 중인지 (`isFollowing`) |

### 본인인증 실명 (#17)

- `member.name`: 포트원 본인인증 실명만 저장. **DB에는 인증 결과 실명**을 넣고, 가입/휴대폰변경 요청의 `name`은 대조용
- 로컬·소셜 가입 body에 `name` 필수. 본인인증 store의 휴대폰·실명과 일치하지 않으면 가입 거부
  - 미인증 → `PHONE_NOT_VERIFIED`
  - 실명 불일치 → `NAME_MISMATCH`
- `nickname`: 표시용 닉네임 (기존 그대로)
- confirm 응답: `verified`, `phoneNumber`, `maskedPhoneNumber`, `name` (FE가 입력란에 채울 수 있음)
- 휴대폰 변경(`PATCH /members/me`): `phoneNumber` + `name` 함께 보내고 동일 검증
- stub: confirm/prepare에 `phoneNumber`·`name`을 넣으면 그 값 사용. 생략 시 `name=테스트사용자`, 번호는 `identity-verification-stub-{phone}` 끝자리
- 기존 DB: `ALTER TABLE member ADD COLUMN name varchar(100) NULL;` (`planwith-infra/db/init/10-member-schema.sql`에도 반영)

### 마이페이지 (#10)

- 인증 사용자: Gateway가 넘긴 `X-Auth-User-Id` (`AuthenticatedUserContext`)
- 로컬 Swagger: 우측 상단 **Authorize** → `X-Auth-User-Id`에 로그인 응답 `user.userId`(memberUuid) 입력 후 me API 호출
- 탈퇴: hard delete 없음. `status=DELETED` + `deleted_at` + Refresh 전부 폐기
- **마이페이지 저장(저장 버튼 1회)**: `PATCH /members/me` (또는 `/members/me/profile` 별칭)
  - body 예: `phoneNumber`, `nickname`, `profileIntro`, `agreements`(선택), `currentPassword`+`newPassword`
  - 휴대폰 변경 시에만 본인인증 완료 필요
  - 응답: `{ member, profile, agreements }`
- 비밀번호: **로컬만**. 소셜 → `PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL`
- 약관: **선택만** 변경 가능. 필수 변경 → `REQUIRED_TERM_NOT_MODIFIABLE`
- `POST .../agreements`, `PATCH .../password`는 단독 수정용 보조 API
- 프로필 이미지: 400×400, jpg/png/webp, 2MB. S3 전 단계로 `stub://profiles/{uuid}.ext` 저장
- Gateway Trust: `GATEWAY_INTERNAL_TOKEN` + `GATEWAY_TRUST_CHECK_ENABLED` (로컬 기본 `false`)

### 팔로우 (#27)

- 테이블: `follow` (`follow_id` PK, `follow_uuid` 외부 식별자, 회원 쌍 UNIQUE, 자기 자신 CHECK 금지)
- 언팔로우: 행 삭제 없음. `is_active=false`. 같은 쌍을 다시 팔로우하면 재활성화
- 자기 자신 팔로우 → `CANNOT_FOLLOW_SELF`
- 대상 회원 없음/탈퇴 → `MEMBER_NOT_FOUND`
- 목록: 활성 팔로우만, 요약 프로필(`memberUuid`, `nickname`, `profileImage`, `profileIntro`, `grade`)
- 페이지: `page` 기본 0, `size` 기본 20, 최대 50
- **팔로우 대상 UUID**: `GET /api/v1/members/search` 목록/닉네임 검색 → `content[].memberUuid` 사용
- 공개 프로필: `GET /api/v1/members/{memberUuid}/profile` → `memberUuid`, `followerCount`, `followingCount`, 로그인 시 `isFollowing`
- 로그인 상태로 목록 조회하면 본인은 제외
- **인증 필요 API** (`POST/DELETE .../follow`, `GET .../follow-status`): Gateway `:8000`에서는 `Authorization: Bearer {accessToken}`
  - Gateway는 클라이언트가 넣은 `X-Auth-User-Id`를 제거한다
  - Member `:8082` 직접 호출일 때만 `X-Auth-User-Id`에 내 memberUuid

### 계정 복구 (#9)

- 아이디 찾기: 본인인증 confirm 후 `phoneNumber`로 조회 → `email` / `maskedEmail` / `loginType`
- 비밀번호 재설정: **로컬 회원만**. 소셜 계정 → `PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL`
- 재설정 코드: `EMAIL_STUB_ENABLED=true`면 로그, `false`면 SMTP

### 토큰 계약 (#7/#8)

- Access Token: 응답 body
- Refresh Token: **HttpOnly Cookie** (`refresh_token`, Path=`/api/v1/auth`) — JSON/로그 금지
- Access 검증: Gateway / Member는 발급·JWKS만
- 소셜 **원클릭 로그인**: `authorizationCode`만 → 기가입 즉시 토큰 / 미가입 `isNewMember=true`
- 소셜 가입: 비밀번호 없음. `password`는 로컬 전용. 닉네임 **2~10자**

---

## 미완료 API (예정)

코어 API + 팔로우(#27) 완료.  
후속(별도 이슈): 좋아요 / 멤버십 / 결제수단

---

## 작업 순서

1. ~~#5 → #6 → #7 → #8 → #9 → #10~~
2. 후속 도메인 이슈는 별도 트래킹

---

## 로컬 스텁 / 실연동 전환

로컬·Swagger API 검증은 stub **켜 둔 상태**가 기본이다.  
실연동은 **하나씩** `false`로 끄고 키를 채운 뒤 아래 순서로 확인한다.  
단위 테스트(`application-test.yaml`)는 stub 고정 — 바꾸지 않는다.

| 구분 | 환경변수 | 기본 | stub 동작 | 실연동 |
| --- | --- | --- | --- | --- |
| 포트원 본인인증 | `PORTONE_STUB_ENABLED` | `true` | id=`identity-verification-stub-{phone}` + optional `phoneNumber`/`name` | 포트원 SDK + API |
| 소셜 OAuth | `SOCIAL_STUB_ENABLED` | `true` | code=`stub:{socialId}:{email}` | 실제 authorization code |
| 이메일 발송 | `EMAIL_STUB_ENABLED` | `true` | `LoggingEmailSender` (서버 로그) | SMTP (`MAIL_*`) |
| JWT 키 | `JWT_*_KEY_PATH` 비움 | ephemeral | 재시작 시 키 변경 가능 | PEM 경로 지정 |
| Gateway Trust | `GATEWAY_TRUST_CHECK_ENABLED` | `false` | Header `X-Auth-User-Id` 직접 | Gateway + `GATEWAY_INTERNAL_TOKEN` |

환경변수 예시: `planwith-infra/env/member.env.example`

### 1) 포트원 본인인증

```env
PORTONE_STUB_ENABLED=false
PORTONE_STORE_ID=store-...
PORTONE_CHANNEL_KEY=channel-key-...
PORTONE_API_SECRET=...
PORTONE_API_BASE_URL=https://api.portone.io
```

확인:

1. `POST /api/v1/auth/phone-verifications` → storeId / channelKey / identityVerificationId  
   스텁이면 body에 `phoneNumber`/`name`을 넣을 수 있다.
2. FE에서 PortOne SDK로 본인인증 완료  
   (로컬 수동 테스트: `http://localhost:8082/portone-identity-test.html` — `src/main/resources/static/`)
3. `POST /api/v1/auth/phone-verifications/confirm`에 SDK가 준 id 전달 (스텁은 `identity-verification-stub-{phone}` + `phoneNumber`/`name`)
4. 응답 `phoneNumber` / `name` 확인
5. 필요 시 포트원 조회 JSON에 `verifiedCustomer.ci` 존재 여부 확인

### 2) 소셜 OAuth

```env
SOCIAL_STUB_ENABLED=false
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
KAKAO_CLIENT_ID=...
KAKAO_CLIENT_SECRET=...
NAVER_CLIENT_ID=...
NAVER_CLIENT_SECRET=...
```

확인:

1. FE에서 provider OAuth → authorization code + redirectUri
2. `POST /api/v1/auth/{provider}/login` 또는 `signup`에 실제 code 전달
3. `stub:...` 코드를 넣으면 실패해야 정상

### 3) 이메일

```env
EMAIL_STUB_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=app-password
MAIL_FROM=your@gmail.com
```

확인:

1. `POST /api/v1/auth/email-verifications` 에 실제 수신 가능한 이메일
2. 메일함에서 6자리 코드 확인 (API 응답·로그에 코드 없음)
3. `POST /api/v1/auth/email-verifications/confirm` 에 그 코드 전달
4. `EMAIL_STUB_ENABLED=true`면 이전처럼 서버 로그에 코드가 남는다

### 4) JWT

```env
JWT_PRIVATE_KEY_PATH=/path/to/private.pem
JWT_PUBLIC_KEY_PATH=/path/to/public.pem
JWT_KEY_ID=planwith-member-...
```

확인: `GET /oauth2/jwks`의 kid가 고정되고, 재시작 후에도 기존 Access Token 검증이 깨지지 않으면 OK.

### 5) Gateway Trust

```env
GATEWAY_TRUST_CHECK_ENABLED=true
GATEWAY_INTERNAL_TOKEN=...
```

확인: Gateway 없이 Member를 직접 치면 `FORBIDDEN`. me API는 Gateway가 붙인 `X-Auth-User-Id`로 동작.

---

## 갱신 규칙

이슈 구현 완료 시:

1. **요약** 상태 변경
2. API를 **완료** 표로 이동
3. README는 `docs/API.md` 링크만 유지 (상세는 본 문서)
