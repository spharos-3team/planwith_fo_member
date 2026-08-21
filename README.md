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

IntelliJ 실행 설정에 환경변수를 넣지 않는다. 모듈 루트 `.env`를 쓴다.

1. infra MySQL이 `127.0.0.1:3307`에서 떠 있어야 합니다 (`planwith-infra` compose).
2. `member_db`에 `terms.is_required` + 약관 시드가 적용돼 있어야 합니다.
3. `.env.example`을 `.env`로 복사한 뒤 필요한 값만 수정합니다. `.env`는 git에 올리지 않습니다.

```bash
cp .env.example .env
export JAVA_HOME="/c/Users/G27/.jdks/ms-17.0.19"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew bootRun
```

- 콘솔에 `[planwith] loaded ...\.env`가 보이면 파일을 읽은 것이다.
- IntelliJ는 Working directory를 모듈 루트(`planwith_fo_member`)로 두고 Application을 실행하면 된다.
- 이메일 인증번호를 메일로 받으려면 `.env`에서 `EMAIL_STUB_ENABLED=false`와 `MAIL_*`를 채운다. 스텁이면 서버 로그에 코드가 남는다.
- Swagger UI (로컬 bootRun): `http://localhost:8082/swagger-ui/index.html`
- Swagger UI (다른 PC / Docker): `http://{SERVER_IP}:8000/swagger-ui.html` → `planwith-fo-member`
- API 진행·완료/미완료 목록: **[docs/API.md](docs/API.md)** (이슈 끝날 때마다 갱신)

다른 PC는 member `:8082`를 직접 호출하지 않는다. 경로는 `Frontend/브라우저 → Gateway :8000 → Eureka → planwith-fo-member:8082` 이다.

## 검증

```bash
./gradlew clean test
./gradlew clean build
```
