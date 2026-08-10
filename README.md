# planwith_fo_member

서버 노트북 Self-hosted Runner 배포 확인용 Spring Boot 서비스입니다.

| 항목 | 값 |
| --- | --- |
| Compose / Eureka 이름 | `planwith-fo-member` |
| 이미지 | `planwith/planwith-fo-member:latest` |
| 포트 | `8082` |
| 배포 확인 | `GET /api/planwith-fo-member/deploy-check` |

## 로컬 실행

```powershell
.\gradlew.bat bootRun
```

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- Deploy check: `http://localhost:8082/api/planwith-fo-member/deploy-check`

## 로그인 테스트

```json
{
  "id": "test-001",
  "pw": "1234"
}
```

| 환경 변수 | 기본값 |
| --- | --- |
| `LOGIN_ID` | `test-001` |
| `LOGIN_PASSWORD` | `1234` |
| `DEPLOY_MARKER` | `planwith-fo-member-deploy-v1` |

## 서버 배포 확인

1. GitHub 레포 생성 후 push (`develop` 또는 `main`)
2. `planwith-infra` compose에 `planwith-fo-member` 등록 후 서버 `C:\planwith\docker-compose.yml` 반영
3. Actions Deploy 성공 확인
4. `http://<서버IP>:8082/api/planwith-fo-member/deploy-check` 응답의 `marker` 확인
