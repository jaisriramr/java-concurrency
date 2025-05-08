# Concurrent File Processing System

A scalable and resilient file-processing microservice built using Spring Boot. It supports handling multiple XLSX file uploads simultaneously and processes the payloads from each file in parallel using thread pools. It also communicates with external APIs and includes robust retry mechanisms and monitoring.

---

## 🚀 Features

- Accepts multiple XLSX file uploads via REST API
- Parses and processes XLSX data concurrently
- Handles external API calls per record
- Rate limiting using Redis
- Resilience with retries, circuit breaker, and time limiter
- Retry queue persistence using Redis
- Custom metrics via Prometheus + Grafana
- Dockerized deployment for production readiness

---

## 🏗️ System Design Overview

### ⚙️ Architecture

```plaintext
+------------------+       +----------------+       +----------------+
|   REST API       +-----> |   XLSX Parser   +-----> |  Payload Queue |
+------------------+       +----------------+       +--------+-------+
                                                           |
                                                           v
                                                  +--------+-------+
                                                  |  Worker Service |
                                                  +--------+-------+
                                                           |
                                      +--------------------+-------------------+
                                      |                    |                   |
                                      v                    v                   v
                              +---------------+   +---------------+   +-----------------+
                              | Rate Limiter  |   | Retry Handler |   | External API    |
                              |   (Redis)     |   | (Redis Queue) |   | Call (with      |
                              |               |   |               |   | Circuit Breaker)|
                              +---------------+   +---------------+   +-----------------+
```
---

## 🛠️ Components

| Component | Responsibility |
|----------|----------------|
| `FileController` | Accepts file upload and initiates processing |
| `XLSXProcessorService` | Parses XLSX and pushes records to main queue |
| `WorkerService` | Consumes tasks from queue asynchronously and calls external API |
| `RateLimiter` | Controls external API call rate using Redis |
| `RetryQueueService` | Handles failed messages and retries with delay |
| `Resilience4j` | Adds fault tolerance: Retry, TimeLimiter, CircuitBreaker |
| `Prometheus + Grafana` | Monitors metrics like success rate, failure rate, duration |

---

### ⚙️ Concurrency Model

- **Thread Pool**: Configurable thread pool using `@Async("taskExecutor")`
- **Queue**: Uses `BlockingQueue<Payload>` to decouple producers and consumers
- **Retry Queue**: Custom Redis-backed retry mechanism for failed requests
- **Rate Limiting**: Redis-backed token bucket limiter to prevent API overload

---

### 🔁 Retry and Resilience Strategy

- **@Retryable**: Retries transient failures on external API calls (IOException, etc.)
- **RetryQueue**: Failed tasks are persisted and retried with exponential backoff
- **CircuitBreaker**: Opens on repeated failures to avoid overloading downstream
- **TimeLimiter**: Prevents blocking threads for long calls

---

### 📊 Metrics and Monitoring

| Metric Name | Description |
|-------------|-------------|
| `file_processing_success_total` | Counter for successful API calls |
| `file_processing_failure_total` | Counter for failed API calls |
| `file_processing_duration_seconds` | Summary of processing time per task |
| `fileprocessing_retry_queue_size` | Gauge showing current retry queue length |
| `executor_queued_tasks{name="taskExecutor"}` | Monitors worker thread queue length |

Use [Grafana](https://grafana.com/) with [Prometheus](https://prometheus.io/) to visualize custom dashboards.

---

### 📦 Deployment Overview

- Dockerized for portability
- Redis and Prometheus are included in `docker-compose.yml`
- Can be deployed in any cloud or local environment
