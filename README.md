# SDA-Pro

Security Incident Response & Threat Mitigation Platform for a Software Design & Architecture semester project.

This repository is demo-oriented: external cybersecurity systems are mocked, while the architecture, design patterns, event flow, persistence, and dashboard are implemented for evaluation.

## Tech Stack

- Java 21 + Spring Boot 3
- React + TypeScript + Vite
- PostgreSQL
- Redis
- RabbitMQ
- Server-Sent Events for realtime dashboard updates
- Docker + Docker Compose
- PlantUML diagrams
- ADR markdown documentation

## Run

```bash
docker-compose up --build
```

Open:

- Dashboard: http://localhost:5173
- Backend API: http://localhost:8080
- RabbitMQ console: http://localhost:15672 using guest / guest

Demo login:

- Email: `analyst@sda-pro.local`
- Password: `password`

## Demo Flow

1. Open the dashboard.
2. Go to Live Alert Stream.
3. Click `Mock Splunk Alert` or call:

```bash
curl -X POST http://localhost:8080/ingest/webhook/splunk \
  -H "Content-Type: application/json" \
  -d "{\"source\":\"splunk\",\"srcIp\":\"203.0.113.10\",\"destHost\":\"finance-db\",\"signature\":\"lateral-movement\",\"severity\":\"CRITICAL\"}"
```

The backend normalizes the alert, enriches it through the chain, checks mock threat intelligence through Adapter + Proxy, groups it with Composite, creates an incident in the New state, publishes events, writes audit logs, and streams updates to the dashboard.

## Architecture Evidence

- SOA: `services/` contains named service boundaries, contracts, and the runnable demo backend.
- MVC: `soc-dashboard/src/controllers`, `models`, and `views`.
- Layered Architecture: backend has `controller`, `service`, `domain`, `repository`, `integration`, and `events` packages.
- Event-Driven Architecture: `EventBusPublisher`, RabbitMQ publishing, SSE dashboard stream, and audit event storage.

## Required Design Patterns

Each pattern class contains `// PATTERN: ...` and `// RATIONALE: ...` comments:

- Singleton
- Factory Method
- Abstract Factory
- Composite
- Facade
- Adapter
- Decorator
- Proxy
- State
- Chain of Responsibility
- Observer
- Strategy

## Tests

Run backend tests:

```bash
cd services/alert-ingestion-service
mvn test
```

Tests cover normalizer factory, enrichment chain, incident states, response strategy selection, threat intel proxy cache, audit event creation, and an end-to-end demo flow.
