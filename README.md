# ShopFlow

Microservices-based online shop backend. Each service owns its database (where needed) and exposes a REST API. Services communicate asynchronously via Kafka.

## Architecture

```
┌──────────────┐  ┌────────────────┐  ┌───────────────┐  ┌─────────────────────┐
│   catalog    │  │   inventory    │  │     order     │  │    notification     │
│    :8085     │  │     :8086      │  │     :8087     │  │        :8088        │
└──────┬───────┘  └───────┬────────┘  └───────┬───────┘  └──────────┬──────────┘
       │                  │                   │                     │
       ▼                  ▼                   ▼                     │
  catalog-db         inventory-db          order-db                 │
    :5433               :5431                :5434                  │
                      ▲                   │                         │
                      │    Kafka :9092    │                         │
                      └───────────────────┴─────────────────────────┘
```

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| **catalog-service** | 8085 | `catalog` (5433) | Products (name, price) |
| **inventory-service** | 8086 | `inventory` (5431) | Stock levels by `productId` |
| **order-service** | 8087 | `orders` (5434) | Orders and line items |
| **notification-service** | 8088 | — | Listens to order events, logs notifications |
| **Kafka** | 9092 | — | Async messaging |

## Kafka flow

```
POST /api/orders
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
- Spring Boot 4.1
- PostgreSQL 17
- Apache Kafka 3.9 (KRaft)
- Docker Compose (databases + Kafka)

## Prerequisites

- JDK 21
- Docker & Docker Compose
- Maven (or use `./mvnw` in each service)

## Getting started

### 1. Start infrastructure

```bash
docker compose up -d
```

### 2. Run services

```bash
cd catalog-service && ./mvnw spring-boot:run
cd inventory-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
cd notification-service && ./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## API

### Catalog — `http://localhost:8085`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by id |
| POST | `/api/products` | Create product |

```json
POST /api/products
{ "name": "Laptop", "price": 999.99 }
```

### Inventory — `http://localhost:8086`

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

### Orders — `http://localhost:8087`

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

### Notification — `http://localhost:8088`

No public business API. Consumes `order.confirmed` / `order.cancelled` and writes notification logs.

## Example flow

1. Create a product in **catalog-service**.
2. Add stock for the same `productId` in **inventory-service**.
3. Create an order in **order-service**.
4. Response status is immediately `NEW`.
5. After ~1–2 seconds: order is `CONFIRMED` or `CANCELLED`, stock updates on success, notification-service logs an EMAIL line.

## Roadmap

- [x] Kafka: `OrderCreated` → inventory reserves stock → order `CONFIRMED` / `CANCELLED`
- [x] Dead Letter Topic (`*-dlt`)
- [x] notification-service
- [ ] API Gateway
- [ ] Docker images for services
- [ ] CI/CD

## Project structure

```
shopflow/
├── catalog-service/
├── inventory-service/
├── order-service/
├── notification-service/
├── docker-compose.yml
└── README.md
```

Each service is an independent Maven project (separate IntelliJ window).
