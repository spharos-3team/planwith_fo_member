# Member API 진행 현황

> 이슈 완료마다 이 문서를 갱신한다.  
> Swagger: `http://localhost:8082/swagger-ui/index.html`  
> 공통 응답: `ApiResponse<T>`  
> 호출 경로: `Frontend → Gateway → Member` (Access 검증은 Gateway)

최종 갱신: 2026-08-13 (#8 구현 완료, PR 전)

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
| ⏳ Todo | #9 | 아이디 찾기·비밀번호 재설정 |
| ⏳ Todo | #10 | 내 회원정보·프로필·약관 동의 |

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

### 토큰 계약 (#7/#8)

- Access Token: 응답 body
- Refresh Token: **HttpOnly Cookie** (`refresh_token`, Path=`/api/v1/auth`) — JSON/로그 금지
- Access 검증: Gateway / Member는 발급·JWKS만
- 소셜 **원클릭 로그인**: `authorizationCode`만 → 기가입 즉시 토큰 / 미가입 `isNewMember=true`
- 소셜 가입: 비밀번호 없음. `password`는 로컬 전용. 닉네임 **2~10자**

### 소셜 원클릭 로그인 (#8) 스텁

```http
POST /api/v1/auth/google/login
{"authorizationCode":"stub:google-123:user@example.com"}
```

- 미가입 → `{ "isNewMember": true, "accessToken": null, ... }` → 본인인증 후 signup
- 기가입 → `{ "isNewMember": false, "accessToken": "...", "user": {...} }` + Refresh Cookie (추가 입력 없음)

---

## 미완료 API (예정)

| Issue | Method | Endpoint | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| #9 | POST | `/api/v1/auth/find-email` | X | 아이디 찾기 |
| #9 | POST | `/api/v1/auth/password/reset-requests` | X | 비밀번호 재설정 요청 |
| #9 | POST | `/api/v1/auth/password/reset` | X | 비밀번호 재설정 |
| #10 | GET | `/api/v1/members/me` | O | 내 회원정보 조회 |
| #10 | PATCH | `/api/v1/members/me` | O | 내 회원정보 수정 |
| #10 | PATCH | `/api/v1/members/me/password` | O | 비밀번호 변경 |
| #10 | DELETE | `/api/v1/members/me` | O | 회원 탈퇴 |
| #10 | GET | `/api/v1/members/me/profile` | O | 내 프로필 조회 |
| #10 | PATCH | `/api/v1/members/me/profile` | O | 내 프로필 수정 |
| #10 | POST | `/api/v1/members/me/profile/image` | O | 프로필 이미지 업로드 |
| #10 | GET | `/api/v1/terms/{termUuid}` | X | 약관 상세 |
| #10 | POST | `/api/v1/members/me/agreements` | O | 약관 동의 등록 |
| #10 | GET | `/api/v1/members/me/agreements` | O | 내 약관 동의 조회 |
| #10 | GET | `/api/v1/members/{memberUuid}` | 내부 | 최소 공개정보 |
| #10 | GET | `/api/v1/members/{memberUuid}/profile` | X | 공개 프로필 |

후속(코어 밖, 별도 이슈): 팔로우 / 좋아요 / 멤버십 / 결제수단

---

## 작업 순서

1. ~~#5 → #6 → #7 → #8~~
2. **다음: 계정 복구 (#9)**
3. me / 프로필 / 약관 (#10)

---

## 로컬 스텁 메모

| 기능 | 스텁 |
| --- | --- |
| 이메일 인증 | 로그에 코드 출력 (`LoggingEmailSender`) |
| 본인인증 | `PORTONE_STUB_ENABLED=true`, id=`identity-verification-stub-{phone}` |
| 소셜 OAuth | `SOCIAL_STUB_ENABLED=true`, code=`stub:{socialId}:{email}` |
| JWT 키 | 경로 미설정 시 ephemeral RSA (로컬만) |

---

## 갱신 규칙

이슈 구현 완료 시:

1. **요약** 상태 변경
2. API를 **완료** 표로 이동
3. README는 `docs/API.md` 링크만 유지 (상세는 본 문서)
