# Member API 진행 현황

> 이슈 완료마다 이 문서를 갱신한다.  
> Swagger: `http://localhost:8082/swagger-ui/index.html`  
> 공통 응답: `ApiResponse<T>`  
> 호출 경로: `Frontend → Gateway → Member` (Access 검증은 Gateway)

최종 갱신: 2026-08-13 (#10 구현 완료, PR 전)

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

---

## 완료된 API

| Issue | Method | Endpoint | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| #1 | POST | `/api/v1/auth/email-verifications` | X | 이메일 인증번호 발송 |
| #1 | POST | `/api/v1/auth/email-verifications/confirm` | X | 이메일 인증번호 확인 |
| #1 | GET | `/api/v1/terms` | X | 약관 목록 |
| #1 | POST | `/api/v1/members` | X | 로컬 회원가입 (201) |
| #3 | POST | `/api/v1/auth/phone-verifications` | X | 본인인증 준비 (포트원 SDK) |
| #3 | POST | `/api/v1/auth/phone-verifications/confirm` | X | 본인인증 완료 확인 |
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
| #10 | GET | `/api/v1/members/me` | O | 내 회원정보 조회 |
| #10 | PATCH | `/api/v1/members/me` | O | **마이페이지 저장** (휴대폰·닉네임·선택약관·비번) |
| #10 | PATCH | `/api/v1/members/me/profile` | O | 위와 동일 (별칭) |
| #10 | DELETE | `/api/v1/members/me` | O | 회원 탈퇴 (soft → `DELETED`) |
| #10 | GET | `/api/v1/members/me/profile` | O | 내 프로필 조회 |
| #10 | POST | `/api/v1/members/me/profile/image` | O | 프로필 이미지 업로드 (stub URL) |
| #10 | GET | `/api/v1/terms/{termUuid}` | X | 약관 상세 |
| #10 | GET | `/api/v1/members/me/agreements` | O | 내 약관 동의 조회 (화면 로드용) |
| #10 | POST | `/api/v1/members/me/agreements` | O | 선택 약관만 단독 변경 (보조) |
| #10 | PATCH | `/api/v1/members/me/password` | O | 비밀번호만 단독 변경 (보조) |
| #10 | GET | `/api/v1/members/{memberUuid}/profile` | X | 공개 프로필 |
| #10 | GET | `/api/v1/members/{memberUuid}` | 내부 | 최소 공개정보 (Gateway Trust) |

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

### 계정 복구 (#9)

- 아이디 찾기: 본인인증 confirm 후 `phoneNumber`로 조회 → `email` / `maskedEmail` / `loginType`
- 비밀번호 재설정: **로컬 회원만**. 소셜 계정 → `PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL`
- 재설정 코드: 이메일 로그 스텁 (`LoggingEmailSender`)

### 토큰 계약 (#7/#8)

- Access Token: 응답 body
- Refresh Token: **HttpOnly Cookie** (`refresh_token`, Path=`/api/v1/auth`) — JSON/로그 금지
- Access 검증: Gateway / Member는 발급·JWKS만
- 소셜 **원클릭 로그인**: `authorizationCode`만 → 기가입 즉시 토큰 / 미가입 `isNewMember=true`
- 소셜 가입: 비밀번호 없음. `password`는 로컬 전용. 닉네임 **2~10자**

---

## 미완료 API (예정)

코어 27개 API 완료.  
후속(코어 밖, 별도 이슈): 팔로우 / 좋아요 / 멤버십 / 결제수단

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
| 포트원 본인인증 | `PORTONE_STUB_ENABLED` | `true` | id=`identity-verification-stub-{phone}` | 포트원 SDK + API |
| 소셜 OAuth | `SOCIAL_STUB_ENABLED` | `true` | code=`stub:{socialId}:{email}` | 실제 authorization code |
| 이메일 발송 | (없음) | 로그 | `LoggingEmailSender` | SMTP 등 미구현 |
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
2. FE에서 PortOne SDK로 본인인증 완료
3. `POST /api/v1/auth/phone-verifications/confirm`에 SDK가 준 id 전달 (stub id 형식 사용 금지)
4. 응답 `phoneNumber` / (가능 시) name 확인
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

### 3) 이메일 (현재 stub만)

스위치 없음. 인증번호·비번 재설정 코드는 **서버 로그**에서 확인.  
실메일 발송은 SMTP/SES 등 추가 연동 전까지 불가.

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
