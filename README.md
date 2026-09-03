# ShopFlow

Microservices-based online shop backend. Each service owns its database and exposes a REST API. Order and inventory are connected asynchronously via Kafka.

## Architecture

```
┌─────────────────┐   ┌──────────────────┐   ┌─────────────────┐
│ catalog-service │   │ inventory-service │   │  order-service  │
│     :8085       │   │      :8086        │   │     :8087       │
└────────┬────────┘   └─────────┬─────────┘   └────────┬────────┘
         │                      │                      │
         ▼                      ▼                      ▼
   catalog-db              inventory-db             order-db
     :5433                    :5431                  :5434
                              ▲                      │
                              │      Kafka :9092     │
                              └──────────────────────┘
                         order.created
                         inventory.reserved / failed
```

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| **catalog-service** | 8085 | `catalog` (5433) | Products (name, price) |
| **inventory-service** | 8086 | `inventory` (5431) | Stock levels by `productId` |
| **order-service** | 8087 | `orders` (5434) | Orders and line items |
| **Kafka** | 9092 | — | Async events between order and inventory |

Services are loosely coupled via `productId` (no cross-service JPA relations).

## Kafka flow

```
POST /api/orders
  → order saved as NEW
  → publish OrderCreated (topic: order.created)

inventory consumes OrderCreated
  → reserve stock
  → publish InventoryReserved (inventory.reserved)
     or InventoryFailed (inventory.failed)

order consumes result
  → CONFIRMED or CANCELLED
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

From the repository root:

```bash
docker compose up -d
```

Starts three Postgres instances and Kafka on `:9092`.

### 2. Run services

Open each service as a separate project and start the Spring Boot application, or from the command line:

```bash
cd catalog-service && ./mvnw spring-boot:run
cd inventory-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
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

## Example flow

1. Create a product in **catalog-service**.
2. Add stock for the same `productId` in **inventory-service**.
3. Create an order in **order-service**.
4. Immediately the response status is `NEW`.
5. After ~1–2 seconds `GET /api/orders/{id}` shows `CONFIRMED` (enough stock) or `CANCELLED` (not enough / missing stock). Stock quantity decreases only on success.

## Roadmap

- [x] Kafka: `OrderCreated` → inventory reserves stock → order `CONFIRMED` / `CANCELLED`
- [ ] Dead Letter Topic
- [ ] API Gateway
- [ ] Docker images for services
- [ ] CI/CD

## Project structure

```
shopflow/
├── catalog-service/
├── inventory-service/
├── order-service/
├── docker-compose.yml
└── README.md
```

Each service is an independent Maven project (separate IntelliJ window).
