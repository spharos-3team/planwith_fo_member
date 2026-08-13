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
- API 진행·완료/미완료 목록: **[docs/API.md](docs/API.md)** (이슈 끝날 때마다 갱신)

## 검증

```bash
./gradlew clean test
./gradlew clean build
```
