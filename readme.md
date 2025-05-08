# Concurrent File Processing System (XLSX to External API Integration)

A scalable and resilient microservice built using Spring Boot to process uploaded `.xlsx` files in parallel. Ideal for systems that need to integrate with third-party APIs per record, such as lead ingestion, catalog syncing, or data enrichment pipelines.

---

## 🚀 Features

### 📂 File Handling
- Accepts multiple `.xlsx` uploads via REST API **in a single request**
- Parses and processes XLSX data concurrently

### ⚙️ Concurrency and Resilience
- Thread pool-based processing with `@Async`
- Rate limiting via Redis (token bucket algorithm)
- Retry handling using Redis-backed queues
- Resilience with Resilience4j: Retry, TimeLimiter, CircuitBreaker

### 📈 Monitoring and Deployment
- Custom metrics using Prometheus and Grafana
- Dockerized for production-readiness
- Redis and Prometheus bundled in `docker-compose.yml`

---

## 🧩 Architecture & Workflow

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
| `FileController` | Accepts file upload(s) and initiates processing |
| `XLSXProcessorService` | Parses each XLSX and pushes records to main queue |
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
