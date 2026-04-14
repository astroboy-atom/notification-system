## Notification System - 실행 문서

### 1. API 문서

#### 기본 정보

- 기본 포트: `8080`
- 포트 환경변수: `API_SERVER_PORT`
- Base URL 예시: `http://localhost:8080`

#### API

<details>
<summary> 알림 접수 API(즉시 발송 시도) </summary>

- Method: `POST`
- Path: `/v1/notifications`

### Request Body

```json
{
  "recipientId": 1,
  "eventId": 100,
  "notificationType": "AFTER_PAID",
  "notificationChanel": "EMAIL"
}
```

### 필드 설명

- `recipientId`: 알림 수신자 ID
- `eventId`: 알림과 연결된 이벤트 ID
- `notificationType`: 알림 유형
- `notificationChanel`: 발송 채널

### 동작

- 서버가 현재 시각(`Instant.now()`)을 예약 시각으로 사용해 알림을 생성합니다.
- 내부적으로 중복 키는 `recipientId:eventId:type:channel` 형식으로 생성됩니다.
- 같은 조합의 알림이 이미 있으면 생성에 실패합니다.

### Response

응답 DTO: `AddNotificationResponse`

```json
{
  "id": 1
}
```

</details>


<details>
<summary>알림 접수 API(예약 발송)</summary>

- Method: `POST`
- Path: `/v1/notifications/scheduled`

### Request Body

```json
{
  "recipientId": 1,
  "eventId": 100,
  "reservedAt": "2026-04-14T12:00:00Z",
  "notificationType": "AFTER_PAID",
  "notificationChanel": "EMAIL"
}
```

### 필드 설명

- `reservedAt`: 예약 발송 시각(ISO-8601 UTC Instant 문자열)
- 나머지 필드는 즉시 알림 생성과 동일합니다.

### 동작

- `reservedAt` 값이 내부 `nextAttemptAt`으로 저장됩니다.
- 중복 판정은 즉시 알림과 동일하게 `recipientId:eventId:type:channel` 조합으로 처리되므로, 예약 시각이 달라도 같은 키면 중복으로 간주됩니다.

### Response

응답 DTO: `AddNotificationResponse`

```json
{
  "id": 2
}
```

</details>


<details>
<summary>알림 상태 조회 API</summary>

- Method: `GET`
- Path: `/v1/notifications/{id}`

### Path Parameter

- `id`: 알림 ID

### Response

응답 DTO: `NotificationResponse`

```json
{
  "id": 1,
  "recipientId": 1,
  "eventId": 100,
  "requestedAt": "2026-04-14T10:15:30.000Z",
  "notificationType": "AFTER_PAID",
  "notificationChanel": "EMAIL",
  "notificationStatus": "PENDING"
}
```

### 응답 필드 설명

- `requestedAt`: 알림 레코드가 생성된 시각
- `notificationStatus`: 현재 처리 상태

</details>


<details>
<summary>사용자 알림 목록 조회 API</summary>

- Method: `GET`
- Path: `/v1/recipients/notifications/{recipientId}`

### Path Parameter

- `recipientId`: 수신자 ID

### Query Parameter

- `isRead` (`Boolean`, 필수): 읽음 여부 필터
- `page` (`int`, 선택): 페이지 번호, 기본값 `0`
- `size` (`int`, 선택): 페이지 크기
- `sort` (`String`, 선택): 예: `sort=id,asc`

### 요청 예시

```http
GET /v1/recipients/notifications/1?isRead=false&page=0&size=20&sort=id,asc
```

### Response

응답 DTO: `Page<NotificationResponse>`

```json
{
  "isLastPage": true,
  "totalPage": 1,
  "data": [
    {
      "id": 1,
      "recipientId": 1,
      "eventId": 100,
      "requestedAt": "2026-04-14T10:15:30.000Z",
      "notificationType": "AFTER_PAID",
      "notificationChanel": "EMAIL",
      "notificationStatus": "PENDING"
    }
  ]
}
```

### 페이지 응답 필드 설명

- `isLastPage`: 마지막 페이지 여부
- `totalPage`: 전체 페이지 수
- `data`: `NotificationResponse` 목록

</details>


<details>
<summary>API enum 설명</summary>

#### `notificationType(알림 유형)`

- `AFTER_PAID`

#### `notificationChanel(알림 채널)`

- `EMAIL`
- `IN_APP`

#### `notificationStatus(알림 상태)`

- `PENDING`
- `IN_PROGRESS`
- `DONE`
- `FAILED`

</details>

### 2. 실행 방법

애플리케이션 실행:

```bash
docker compose -f docker/docker-compose.app.yml up --build
```

MySQL 더미 데이터는 [docker/mysql/init.sql](/Users/haneul/Desktop/2026/notification-server/docker/mysql/init.sql)로 초기화됩니다.

구성:

- `mysql`: notification DB
- `api`: 1개 인스턴스
- `publish-1`: publish worker 1
- `publish-2`: publish worker 2

접속 포트:

- API: `http://localhost:8080`
- MySQL: `localhost:3308`
    - DB : `notification`
    - ID : `root`
    - PW : `root`
- Publish Worker :  인스턴스는 외부 포트를 열지 않고, 앱/management 포트 모두 랜덤으로 뜹니다.

###  
