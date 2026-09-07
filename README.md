# ShopFlow

Microservices-based online shop backend. Each service owns its database (where needed). Clients enter through an API Gateway. Services communicate asynchronously via Kafka.

## Architecture

```
                         ┌─────────────────┐
                         │ gateway-service │
                         │     :8080       │
                         └────────┬────────┘
            ┌─────────────────────┼─────────────────────┐
            ▼                     ▼                     ▼
     catalog :8085         inventory :8086         order :8087
            │                     │                     │
            ▼                     ▼                     ▼
       catalog-db            inventory-db           order-db
         :5433                  :5431                 :5434
                              ▲                   │
                              │    Kafka :9092    │
                              └───────────────────┴──────────► notification :8088
```

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| **gateway-service** | 8080 | — | API Gateway (routing) |
| **catalog-service** | 8085 | `catalog` (5433) | Products (name, price) |
| **inventory-service** | 8086 | `inventory` (5431) | Stock levels by `productId` |
| **order-service** | 8087 | `orders` (5434) | Orders and line items |
| **notification-service** | 8088 | — | Listens to order events, logs notifications |
| **Kafka** | 9092 | — | Async messaging |

## Kafka flow

```
POST /api/orders (via gateway :8080)
  → order NEW → order.created

inventory: reserve stock
  → inventory.reserved  or  inventory.failed
  → on technical failure after retries → order.created-dlt

order:
  → CONFIRMED → order.confirmed
  → CANCELLED → order.cancelled

notification:
  → EMAIL log for confirmed / cancelled
```

## Tech stack

- Java 21
- Spring Boot 4.1 (gateway on Boot 4.0 + Spring Cloud Gateway)
- PostgreSQL 17
- Apache Kafka 3.9 (KRaft)
- Docker Compose (databases, Kafka, and all services)

## Prerequisites

- JDK 21 (only if running services locally without Docker)
- Docker & Docker Compose
- Maven / `./mvnw` (local runs)

## Getting started

### Option A — full stack in Docker (recommended)

```bash
docker compose up -d --build
```

Then use the gateway:

```text
http://localhost:8080/api/products
http://localhost:8080/api/stock
http://localhost:8080/api/orders
```

### Option B — infrastructure in Docker, apps in IDE

```bash
docker compose up -d catalog-db inventory-db order-db kafka
```

Run each Spring Boot app locally (without `SPRING_PROFILES_ACTIVE=docker`).

## API (through gateway)

Base URL: `http://localhost:8080`

### Catalog

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by id |
| POST | `/api/products` | Create product |

```json
POST /api/products
{ "name": "Laptop", "price": 999.99 }
```

### Inventory

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stock` | List all stock records |
| GET | `/api/stock/{productId}` | Get stock for product |
| POST | `/api/stock` | Create stock record |
| PUT | `/api/stock/{productId}` | Update quantity |
| DELETE | `/api/stock/{productId}` | Delete stock record |

```json
POST /api/stock
{ "productId": 1, "quantity": 50 }
```

### Orders

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/{id}` | Get order by id |
| POST | `/api/orders` | Create order |
| PATCH | `/api/orders/{id}/cancel` | Cancel order |

```json
POST /api/orders
{
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

Order statuses: `NEW` → `CONFIRMED` or `CANCELLED` (via Kafka).

### Notification

No public business API. Consumes `order.confirmed` / `order.cancelled` and writes notification logs.

## Example flow

1. `POST /api/products` via gateway.
2. `POST /api/stock` for the same `productId`.
3. `POST /api/orders`.
4. Response status is immediately `NEW`.
5. After ~1–2 seconds: order is `CONFIRMED` or `CANCELLED`, stock updates on success, notification logs an EMAIL line.

## Roadmap

- [x] Kafka: `OrderCreated` → inventory reserves stock → order `CONFIRMED` / `CANCELLED`
- [x] Dead Letter Topic (`*-dlt`)
- [x] notification-service
- [x] API Gateway
- [x] Docker images for services
- [x] CI/CD (Jenkinsfile: parallel `mvn test` + package)

## Project structure

```
shopflow/
├── gateway-service/
├── catalog-service/
├── inventory-service/
├── order-service/
├── notification-service/
├── docker-compose.yml
└── README.md
```

Each service is an independent Maven project (separate IntelliJ window).
Docker profile: `application-docker.properties` / `application-docker.yml`.
