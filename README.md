# Spring Boot Kotlin Coroutines Demo

A demo CRUD application built with **Spring Boot 3**, **Kotlin 2**, and **Kotlin Coroutines** using Maven.
The application demonstrates how coroutines integrate with a reactive Spring WebFlux + R2DBC stack and provides
runtime control to **enable or disable coroutine-based execution** without restarting the application.

---

## Tech Stack

| Technology | Version |
|---|---|
| Spring Boot | 3.4.2 |
| Kotlin | 2.1.10 |
| kotlinx-coroutines | 1.10.1 |
| Spring WebFlux | (via Spring Boot) |
| Spring Data R2DBC | (via Spring Boot) |
| H2 (in-memory) | (via Spring Boot) |
| Java | 17 |

---

## Project Structure

```
src/main/kotlin/com/example/demo/
├── DemoApplication.kt           # Spring Boot entry point
├── config/
│   └── CoroutineManager.kt      # Runtime coroutine enable/disable manager
├── controller/
│   └── ProductController.kt     # REST endpoints (CRUD + coroutine control)
├── model/
│   └── Product.kt               # Product entity (R2DBC @Table)
├── repository/
│   └── ProductRepository.kt     # CoroutineCrudRepository
└── service/
    └── ProductService.kt        # Business logic with coroutine-aware execution
```

---

## Running the Application

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

---

## API Reference

### Product CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | List all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

**Example – create a product:**

```bash
curl -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Laptop","description":"High-performance laptop","price":1299.99}'
```

### Coroutine Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/coroutines` | Get current coroutine execution status |
| `POST` | `/api/coroutines/enable` | Enable coroutine-based async execution |
| `POST` | `/api/coroutines/disable` | Disable coroutines (synchronous path) |

**Example – disable coroutines at runtime:**

```bash
curl -X POST http://localhost:8080/api/coroutines/disable
# {"enabled":false}

curl -X POST http://localhost:8080/api/coroutines/enable
# {"enabled":true}
```

---

## How Coroutine Management Works

`CoroutineManager` is a Spring-managed component backed by an `AtomicBoolean`.

* **Enabled (default):** The service layer dispatches work on `Dispatchers.IO` using `withContext`, simulating real-world async I/O with a configurable delay. The `Flow<Product>` from the repository is consumed with per-item suspension via `onEach { delay(...) }`.
* **Disabled:** Operations still use the coroutine-based `CoroutineCrudRepository` (suspend functions) but skip the extra context switch and artificial delay, making behaviour equivalent to a synchronous call.

This lets you observe the timing difference between the two modes by comparing response latencies.

---

## Running Tests

```bash
mvn test
```

Tests cover all CRUD endpoints and the coroutine enable/disable lifecycle using `WebTestClient` and an in-memory H2 database.

---

## Key Concepts Demonstrated

* **`CoroutineCrudRepository`** – Spring Data repository exposing `suspend` functions instead of `Mono`/`Flux`.
* **`suspend` controller methods** – Spring WebFlux bridges coroutine suspension points transparently.
* **`Flow<T>`** – Reactive streaming collection returned from `GET /api/products`.
* **`withContext(Dispatchers.IO)`** – Offloading work to the I/O dispatcher inside a coroutine.
* **`kotlinx.coroutines.flow.onEach`** – Applying async side-effects per element in a Flow.
* **Runtime feature flag** – Toggling async behaviour without redeployment.
