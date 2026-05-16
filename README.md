# Risk Score Service

Сервис контроля рисков развития схем данных в событийно-управляемых микросервисах.

`risk-score-service` работает поверх Schema Registry и не заменяет его. Сервис анализирует изменения схем, считает `riskScore`, возвращает `riskFactors`, принимает `governanceDecision`, формирует `recommendations` и `structuredRecommendations`, а также сохраняет результаты анализа в PostgreSQL.

## 1. Назначение проекта

В event-driven микросервисах одна схема сообщения может использоваться несколькими producer- и consumer-сервисами. Изменение схемы без контроля может привести к ошибкам десериализации, нарушению контрактов и сбоям зависимых сервисов.

`Risk Score Service` выступает как governance-надстройка над Schema Registry:

- анализирует совместимость старой и новой схемы
- строит diff изменений
- оценивает риск
- объясняет риск через `riskFactors`
- принимает `governanceDecision`
- может выступать как controlled schema promotion gateway

## 2. Архитектура

### Компоненты

- **Schema Registry**
  - хранит `subjects`, версии схем и текст схем
- **PostgreSQL**
  - хранит history analysis, usage map, impact и audit-данные
- **risk-score-service**
  - выполняет compatibility analysis, diff analysis, risk scoring, impact analysis, governance decision и формирует рекомендации
- **Kafka / ZooKeeper**
  - используются для локального запуска Schema Registry в Docker

### Текстовая схема

Пользователь  
↓  
`risk-score-service`  
↓  
Schema Registry

Отдельно:

`risk-score-service`  
↓  
PostgreSQL для history / usage / audit

## 3. Основные возможности

- регистрация схем в Schema Registry через сервис
- standalone raw-анализ двух схем
- анализ изменений между версиями из Schema Registry
- controlled promotion flow
- explainable risk engine
- `riskFactors`
- impact-анализ producer/consumer зависимостей
- `governanceDecision`
- `decisionExplanation`
- `structuredRecommendations`
- history / audit
- usage graph

## 4. Режимы работы API

### Raw-анализ

Endpoint:

`POST /api/v1/checks`

Назначение:

- сравнивает `oldSchema` и `newSchema` напрямую
- не использует Schema Registry
- не публикует схему
- не требует `subject` и `version`
- подходит для быстрой предварительной проверки

### Versioned-анализ

Endpoint:

`POST /api/v1/subjects/{subject}/checks`

Назначение:

- берет `oldVersion` и `newVersion` из Schema Registry
- или сравнивает `oldVersion` из Registry с `newSchema` как draft
- не публикует новую схему
- сохраняет результат анализа в history

### Ручная регистрация схемы

Endpoint:

`POST /api/v1/subjects/{subject}/versions`

Назначение:

- регистрирует схему в Schema Registry через сервис
- не выполняет governance-gating
- подходит для административных и демонстрационных сценариев

### Controlled promotion

Endpoint:

`POST /api/v1/subjects/{subject}/promotions`

Назначение:

- основной безопасный flow
- сервис анализирует новую схему
- регистрирует ее в Schema Registry только если `governanceDecision = ALLOW`

## 5. Ключевые поля ответа

- `compatible` — флаг формальной совместимости
- `issues` — список проблем совместимости
- `diff` — diff между схемами
- `riskScore` — числовая оценка риска
- `riskLevel` — уровень риска
- `decision` — technical decision
- `governanceDecision` — предметное governance-решение
- `decisionExplanation` — объяснение governance-решения
- `riskFactors` — факторы, из которых складывается риск
- `recommendations` — текстовые рекомендации
- `structuredRecommendations` — машиночитаемые рекомендации
- `impact` — результат impact-анализа
- `impactGraph` — граф зависимостей

### Разница между `decision` и `governanceDecision`

`decision`:

- technical decision из `RiskResult`
- значения: `ALLOW / WARN / BLOCK`

`governanceDecision`:

- предметное governance-решение
- значения: `ALLOW / ALLOW_WITH_CAUTION / REQUIRE_CONSUMER_UPGRADE_FIRST / REJECT / SUGGEST_NEW_SUBJECT`

## 6. Controlled promotion policy

- `ALLOW` → схема регистрируется в Schema Registry
- `ALLOW_WITH_CAUTION` → схема не регистрируется автоматически, требуется ручное согласование
- `REQUIRE_CONSUMER_UPGRADE_FIRST` → схема не регистрируется, сначала нужно обновить consumer-сервисы
- `REJECT` → схема не регистрируется
- `SUGGEST_NEW_SUBJECT` → схема не регистрируется в текущий `subject`

## 7. Быстрый запуск через Docker

```bash
docker compose up --build
```

или:

```bash
docker compose up -d --build
```

Проверки:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Schema Registry: `http://localhost:8081/subjects`

Остановка:

```bash
docker compose down
```

Полная очистка данных:

```bash
docker compose down -v
```

## 8. Переменные окружения

Основные переменные:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SCHEMA_CATALOG_MODE`
- `CONFLUENT_SCHEMA_REGISTRY_URL`

По умолчанию используется:

```env
SCHEMA_CATALOG_MODE=confluent
```

## 9. Примеры API

### A. Raw-анализ safe optional field

`POST /api/v1/checks`

Пример:

```json
{
  "schemaType": "AVRO",
  "compatibilityMode": "BACKWARD",
  "oldSchema": "{\"type\":\"record\",\"name\":\"UserCreated\",\"namespace\":\"ru.kpfu.events\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"email\",\"type\":\"string\"}]}",
  "newSchema": "{\"type\":\"record\",\"name\":\"UserCreated\",\"namespace\":\"ru.kpfu.events\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"email\",\"type\":\"string\"},{\"name\":\"middleName\",\"type\":[\"null\",\"string\"],\"default\":null}]}"
}
```

### B. Controlled promotion safe schema

`POST /api/v1/subjects/{subject}/promotions`

```json
{
  "schemaType": "AVRO",
  "compatibilityMode": "BACKWARD",
  "schemaText": "{\"type\":\"record\",\"name\":\"UserCreated\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"}]}",
  "description": "safe evolution",
  "createdBy": "postman-demo"
}
```

### C. Controlled promotion blocked breaking schema

Пример ответа:

```json
{
  "subject": "user-created",
  "registered": false,
  "registeredVersion": null,
  "schemaRegistryId": null,
  "registrationStatus": "BLOCKED_BY_GOVERNANCE",
  "registrationMessage": "Schema was not registered because governance policy rejected the change",
  "analysis": {
    "riskFactors": [],
    "structuredRecommendations": []
  }
}
```

## 10. История анализов

История хранит:

- `riskScore`
- `riskLevel`
- technical `decision`
- `governanceDecision`
- `decisionExplanation`
- `riskFactors`
- `recommendations`
- `structuredRecommendations`
- `impact`
- `diff`
- `issues`

## 11. Ограничения текущей версии

- основной фокус реализации — Avro
- зависимости для JSON Schema / Protobuf оставлены как возможное направление расширения
- `LocalSchemaCatalog` сохранен как legacy/development-only режим
- raw-анализ не выполняет impact/governance по `subject`, потому что не имеет контекста usage map
