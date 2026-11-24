# Messages Service

Messages Service. Java 21, Spring Boot 3.5, PostgreSQL

## Запуск (Docker)

```bash
docker compose up --build -d
```

| Контейнер | Порт | Назначение |
|-----------|------|------------|
| `messages-postgres` | `5433` | PostgreSQL (`messages_db`) |
| `messages-service` | `9001` | Messages Service |

Сервис: `http://localhost:9001`

Локально без образа сервиса:

```bash
docker compose up -d postgres
cd message-service && mvn spring-boot:run
```

## API (`/api/v1`)

Все эндпоинты требуют `Authorization: Bearer <token>`.

| Method | Path | Описание |
|--------|------|----------|
| `POST` | `/messages` | создать (роль `producer`) |
| `GET` | `/messages/{id}` | получить сообщение |
| `PATCH` | `/messages/{id}` | изменить (автор) |
| `DELETE` | `/messages/{id}` | soft-delete (автор / administrator) |
| `GET` | `/users/{id}/messages` | сообщения пользователя |
| `PUT` | `/users/me/following/{id}` | подписаться |
| `DELETE` | `/users/me/following/{id}` | отписаться |
| `GET` | `/users/{id}/following` | на кого подписан |
| `GET` | `/users/{id}/followers` | подписчики |
| `GET` | `/feed?limit=&offset=` | лента |

### Создание сообщения

```http
POST /api/v1/messages
Authorization: Bearer <token>
Content-Type: application/json

{ "text": "Сегодня вышел новый выпуск" }
```

### Лента

```http
GET /api/v1/feed?limit=20&offset=0
Authorization: Bearer <token>
```

```json
{
  "items": [
    {
      "id": 3,
      "text": "...",
      "author": { "id": 2, "firstname": "Дмитрий", "lastname": "Кузнецов" },
      "created_at": "2026-09-24T11:00:00"
    }
  ],
  "limit": 20,
  "offset": 0
}
```

## Структура

```
lr_8/
├── docker-compose.yml
├── README.md
└── message-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/messagesservice/
        ├── config/
        ├── controller/
        ├── service/
        ├── client/
        ├── dao/
        ├── dto/
        ├── model/
        ├── util/
        └── exception/
```
