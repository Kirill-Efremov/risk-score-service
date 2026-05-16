# Docker запуск

## Что поднимает `docker compose`

По умолчанию `docker compose up --build` поднимает полный локальный контур:

- PostgreSQL
- ZooKeeper
- Kafka
- Confluent Schema Registry
- `risk-score-service`

Проект работает в registry-first режиме:

- Schema Registry хранит `subjects`, `versions` и текст схем
- PostgreSQL хранит историю анализов, usage map, impact и audit-данные

## Быстрый старт

```bash
docker compose up --build
```

или в фоне:

```bash
docker compose up -d --build
```

## Проверка сервисов

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Schema Registry subjects: `http://localhost:8081/subjects`

## Очистка данных

Остановка контейнеров:

```bash
docker compose down
```

Полная очистка вместе с volume PostgreSQL:

```bash
docker compose down -v
```

## Переменные окружения

Основные переменные:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SCHEMA_CATALOG_MODE`
- `CONFLUENT_SCHEMA_REGISTRY_URL`
- `CONFLUENT_SCHEMA_REGISTRY_USERNAME`
- `CONFLUENT_SCHEMA_REGISTRY_PASSWORD`
- `CONFLUENT_SCHEMA_REGISTRY_API_KEY`
- `CONFLUENT_SCHEMA_REGISTRY_API_SECRET`

По умолчанию используется:

```env
SCHEMA_CATALOG_MODE=confluent
CONFLUENT_SCHEMA_REGISTRY_URL=http://schema-registry:8081
```

## Registry-first режим

В текущей версии проекта:

- Schema Registry является источником истины для схем
- `risk-score-service` анализирует и контролирует публикацию схем
- LocalSchemaCatalog оставлен только как legacy/development-only режим

## Где хранятся данные

### Schema Registry

Хранит:

- `subjects`
- версии схем
- текст схем

### PostgreSQL

Хранит:

- историю анализов
- usage map producer/consumer сервисов
- impact и audit-данные

## Основные flow

- Ручная регистрация схемы:
  - `POST /api/v1/subjects/{subject}/versions`
- Controlled promotion:
  - `POST /api/v1/subjects/{subject}/promotions`

Controlled promotion сначала анализирует новую схему относительно latest-версии в Schema Registry и регистрирует её только если `governanceDecision = ALLOW`.

## Частые проблемы

### Schema Registry пустой

Если `GET http://localhost:8081/subjects` возвращает `[]`, это нормально после:

```bash
docker compose down -v
```

### Полная очистка

Чтобы удалить контейнеры и данные PostgreSQL:

```bash
docker compose down -v
```

### Ошибки registration/promotion endpoint-ов

Проверьте логи приложения:

```bash
docker compose logs -f risk-score-service
```

### Ошибка сборки Docker на `gradlew`

Проверьте:

- line endings
- наличие `gradlew`
- настройки `.gitattributes`, если будете добавлять их позже
