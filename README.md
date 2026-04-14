## Notification System

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

MySQL 더미 데이터는 [docker/mysql/init.sql]()로 초기화됩니다.

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

### 3. 테스트 방법

이벤트 ID와 수신자 ID는 1부터 10 까지 존재합니다.
이때, 수신자 6부터는 정상 발송하며, 1부터 5까지는 에러 재현을 수행합니다.(direct 모듈의 Scenario 클래스 참고)

- 수신인 ID가 1이면, retryable exception -> 2번째 재시도에 성공
- 수신인 ID가 2이면, retryable exception -> 모든 재시도에서 실패(final fail)
- 수신인 ID가 3이면, none retryable exception
- 수신인 ID가 4이면, ambiguous call exception
- 수신인 ID가 5이면, process crash -> sender 수행 후 ambiguous call exception

### 4. 모듈 설명

- apps:api = API 기능을 수행합니다.
- apps:publish = 폴링 퍼블리셔 기능을 수행하는 독립 프로세스 워커 구현이 존재합니다.
- apps:consumer = (미구현) 추후 알림 요청 접수가 메시지 브로커를 통해서 들어오는 경우나, supplier에서 브로커로 전달하는 경우 사용하는 모듈입니다.
- apps:enums = 프로젝트의 공통 enum이 존재합니다.
- storage:db = orm 엔티티 정의 및 접근 기능을 제공합니다.
- supplier:common = apps:publish에서 사용하는 supplier(직접 전송, 메시지 큐로 위임)에 대한 추상화와 공통 예외가 존재합니다.
- supplier:direct = (mock 구현 + 시나리오 트랩 존재) 직접 발송 채널에 알림 전송을 수행합니다.
- supplier:mq = (mock 구현) 직접 발송하는 것이 아닌, 메시지 브로커에게 전송합니다.

### 5. AI 활용 범위

> 프로덕션 코드의 기본 골격은 직접 작성하고, AI에게 테스트 코드 작성과 엣지 케이스를 커버하는지 피드백받는 사이클로 개발했습니다.

- 테스트 코드 작성
- 에러 로그 분석
- 설계 검토
- 프로덕션 코드 정리
- API 문서 작성

### 6. 비동기 처리 구조 및 재시도 정책

<img width="1156" height="567" alt="image" src="https://github.com/user-attachments/assets/3471efbd-1cb0-4b6b-bf52-bc6da81fec8b" />

- 요청을 저장하고 알림을 transaction after commit event로 발행한다면 알림 발송에 대한 무한 retry하다, 프로세스가 크래시되는 상황에서 pending인 채로 머물기 떄문에 알림 요청을 db에서 조회하는 별도의 스케줄링 스레드를 도입했습니다. (폴링 퍼블리셔, publish 모듈의 NotificationPublisher)
  - 폴링 퍼블리셔는 batch 단위로 pending 알림을 가져와 in_progress로 마킹합니다. (tx1, claim 단계)
  - 해당 스레드에서 claim한 알림을 처리합니다. (tx2, send 단계)
    - send의 대상은 supplier로 명명했고, 이는 직접 알림 채널에 발송하는 direct와 브로커로 전송하는 mq 모듈에서 구현을 제공합니다.
    - 요청 성공인 경우에는 done으로 마킹합니다.
    - RetryableException(일시적 에러)인 경우에는 지수 백오프 재시도를 수행하기 위해서 pending으로 다시 마킹하고, 다음 재시도 시점을 저장합니다. (retry 카운트를 모두 소모하면, 최종 실패 처리합니다.)
    - Non-RetryableException(영구적 에러)인 경우에는 failed로 마킹합니다.
    - AmbiguousCallException(Read Timeout 등) 처럼 호출이 명확하지 않는 경우에는 in_progress 상태를 유지하고 리커버리 스레드가 처리합니다.
    - Non-RetryableException이 발생한 경우, 커스텀 메트릭 카운터를 증가시켜, 수동 조치를 위한 가시성을 확보했습니다. (AmbiguousCallException은 자동 복구지만, 시스템 이상 증상에 가깝다고 판단하여 해당 예외도 카운팅했습니다.)
- 리커버리 스레드가 별도로 동작해 주기적으로 in_progress timeout된 요청을 pending으로 복구하려고 시도합니다. (publish 모듈의 NotificationRecoveryProcessor)
  - supplier의 멱등성 지원 여부에 따라서, 그리고 supplier에 요청이 이미 전송됐는지 알 수 있음에 따라서 in_progress 요청과 같은 호출 불확정 상태를 exactly-once에 가깝거나, 최소한 at-least-once는 보장하도록 구현했습니다.
  - 해당 스레드가 동작하는 상황은 DB 장애(일시적, 영구적)나 프로세스 크래시에서 발생할 수 있습니다.

### 7. 요구사항 해석 및 개선 의견

- 퍼블리셔의 전송 과정이 길어지는 경우 리커버리 스레드에서 정상 케이스 in_progress 요청을 캐치할 수 있습니다. 이걸 해결하기 위해서 batch size와 timeout의 값의 균형으로 문제를 해결하고 있는데요. 이런 깨지기 쉬운 엔지니어링은 운영 환경에서 위험하다고 생각합니다. 이 부분을 개선하고 싶은데, 시간이 부족하여 막연한 해결 방법(하트비트, 상태 추가)만 떠오르는 상태입니다. 
