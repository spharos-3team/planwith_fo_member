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

환경변수 없이도 기본 접속은 `member_user` / `member_db` / `3307` 입니다.
Docker 배포 시에는 `env/member.env`로 `DB_URL` 등을 덮어씁니다.

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`

## Issue #1 API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/v1/auth/email-verifications` | 이메일 인증번호 발송 |
| POST | `/api/v1/auth/email-verifications/confirm` | 이메일 인증번호 확인 |
| GET | `/api/v1/terms` | 약관 목록 |
| POST | `/api/v1/members` | 로컬 회원가입 (201) |

공통 응답: `ApiResponse` (`success`, `data`, `error{code,message,fieldErrors}`, `timestamp`)

이메일 인증코드는 현재 로그로 출력됩니다 (`LoggingEmailSender`).

## 검증

```powershell
.\gradlew.bat clean test
.\gradlew.bat clean build
```
