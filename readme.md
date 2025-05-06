# Concurrent File Processing System

A scalable and resilient file-processing microservice that supports concurrent uploads, API calls, and retry/resiliency mechanisms using Spring Boot, Redis, Docker, and Prometheus-Grafana monitoring.

---

## 🚀 Features

- Concurrent CSV processing with thread-pool-backed queue system
- Asynchronous worker execution
- Retry with backoff, circuit breaker, and time limiter
- Redis-based rate limiting and retry queue persistence
- Metrics with Prometheus and Grafana
- Dockerized for production deployment

---

## 🏗️ System Design Overview

### ⚙️ Architecture

```plaintext
Client
  |
  v
[CSV Upload API]  --->  [CSV Processor Service]
                                |
                                v
                    [Concurrent Task Queue]
                                |
                                v
                        [Worker Service]
                                |
               -----------------------------------
               |                                 |
     [External API Call]            [Retry Logic & Redis Queue]
               |
               v
         [Business Database]
