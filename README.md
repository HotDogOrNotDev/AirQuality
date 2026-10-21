# Air Quality

Telegram-бот на Spring Boot: пользователь пишет город, бот спрашивает [WAQI](https://aqicn.org/api/) и отвечает концентрацией PM2.5, температурой, влажностью и короткой рекомендацией.

## Как это устроено

```text
Telegram  →  TelegramBot  →  TextMessageHandler / CallBackQueryHandler
                │
                ├─ BotDialog        тексты и клавиатура
                └─ BotMessenger     отправка сообщений
                          │
                          └─ AirQualityService  →  OpenFeign  →  api.waqi.info
                                                          │
Prometheus  ←  /actuator/prometheus  ←  Spring Boot
     │
     └─ Grafana
```

`TelegramBot` только принимает обновления и один раз отдаёт себя в `BotMessenger`. Обработчики и сервис качества воздуха бота напрямую не знают, поэтому цикла зависимостей нет.

Город нужно вводить на английском: так его понимает WAQI (`moscow`, `london`).

## Запуск

Нужны Docker и токены:

- Telegram — у [@BotFather](https://t.me/BotFather)
- WAQI — на [aqicn.org/data-platform/token](https://aqicn.org/data-platform/token)

```bash
cp .env.example .env
```

В `.env` заполните `BOT_TOKEN` и `WAQI_TOKEN`. Файл в git не попадает.

```bash
docker compose up -d --build
```

Контейнер стартует с профилем `prod`: поверх `application.yaml` подключается `application-prod.yaml` (без SQL-логов). Локальный запуск без этого профиля остаётся подробнее в логах.

Порты открыты только на `127.0.0.1`, снаружи машины их не видно:

| Куда | Адрес |
| --- | --- |
| Метрики приложения | http://localhost:8082/actuator/prometheus |
| Health | http://localhost:8082/actuator/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

В Grafana логин по умолчанию `admin` / `admin`. Дашборд лежит в папке AirQuality: статус приложения, CPU, память JVM, частота и 95-й перцентиль HTTP-запросов. Prometheus забирает метрики по внутренней сети Compose (`air-quality:8080`), история Prometheus и Grafana хранится в томах и переживает пересоздание контейнеров.

Без Docker:

```bash
./mvnw spring-boot:run
```

Spring Boot читает `.env` из корня проекта (`spring.config.import`). Токены в yaml и в код не подставляются.

## Тесты

```bash
./mvnw test
```

Проверяются разбор ответа WAQI и пороги рекомендаций по PM2.5: 15, 40, 65, 150, 250 и 350. Контекст Spring и Telegram в тестах не поднимаются.
