# Docker Compose

애플리케이션 실행:

```bash
docker compose -f docker/docker-compose.app.yml up --build
```

MySQL 더미 데이터는 [docker/mysql/init.sql](/Users/haneul/Desktop/2026/notification-server/docker/mysql/init.sql)로 초기화됩니다.
로컬 MySQL 데이터는 `docker/mysql/data`에 저장되며 Git에는 포함되지 않습니다.

구성:

- `mysql`: notification DB
- `api`: 1개 인스턴스
- `publish-1`: publish worker 1
- `publish-2`: publish worker 2

접속 포트:

- API: `http://localhost:8080`
- API Actuator: `http://localhost:8081/actuator/prometheus`
- MySQL: `localhost:3308`

`publish` 인스턴스는 외부 포트를 열지 않고, 앱/management 포트 모두 랜덤으로 뜹니다.
