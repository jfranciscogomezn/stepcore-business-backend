# StepCore Business — Backend API

REST API for **time tracking and payroll configuration**, deployed as a separate microservice from `stepcore-security-backend`.

Both services share the **same PostgreSQL database** (`stepcore_security`) but run as independent processes with separate Flyway migration histories.

---

## Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Database | PostgreSQL 15 (shared with security service) |
| Migrations | Flyway (`flyway_schema_history_business`) |
| Auth | Stateless JWT validation (tokens issued by security service) |
| Port | **8081** (security uses 8080) |

---

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL running (same instance as security backend)
- Security backend migrations applied (`tenants`, `stepcore_app` role, etc.)

Start the shared database from the monorepo root:

```bash
docker compose up -d postgres
```

---

## Configuration

Copy `.env.example` to `.env` and align values with the security backend:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_NAME` | `stepcore_security` | Shared database name |
| `DB_APP_USER` | `stepcore_app` | Runtime role (RLS enforced) |
| `JWT_SECRET` | *(must match security)* | HMAC secret for token validation |
| `PORT` | `8081` | Business service HTTP port |

---

## Run locally

```bash
mvn spring-boot:run
```

Swagger UI: http://localhost:8081/swagger-ui.html

Obtain a JWT from the security service (`POST http://localhost:8080/api/v1/auth/login`) and send it as `Authorization: Bearer <token>` on business endpoints.

---

## API (payroll configuration)

| Method | Path | Role |
|--------|------|------|
| `GET` | `/api/v1/config/payroll/{year}` | ADMIN |
| `PUT` | `/api/v1/config/payroll/{year}` | ADMIN |
| `GET` | `/api/v1/config/holidays/{year}` | ADMIN |
| `POST` | `/api/v1/config/holidays` | ADMIN |
| `DELETE` | `/api/v1/config/holidays/{id}` | ADMIN |

## API (employee configuration)

| Method | Path | Role |
|--------|------|------|
| `POST` | `/api/v1/employees` | ADMIN |
| `GET` | `/api/v1/employees` | ADMIN |
| `GET` | `/api/v1/employees/{id}` | ADMIN |
| `PUT` | `/api/v1/employees/{id}` | ADMIN |

---

## Tests

```bash
mvn test
```

Integration tests use Testcontainers (PostgreSQL 15).

---

## Architecture note

```
┌─────────────────────┐     ┌─────────────────────┐
│ Security Backend    │     │ Business Backend    │
│ :8080               │     │ :8081               │
│ issues JWT + roles  │     │ validates JWT       │
└─────────┬───────────┘     └─────────┬───────────┘
          │                           │
          └───────────┬───────────────┘
                      ▼
            PostgreSQL stepcore_security
     flyway_schema_history          (security tables)
     flyway_schema_history_business (payroll_configs, holidays)
```
