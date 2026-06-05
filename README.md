# Utility Billing System (ubsystem)

Spring Boot backend for WASAC/REG utility billing — customers, meters, readings, tariffs, bills, payments, and automated notifications.

## Prerequisites

- Java 25+
- Maven 3.9+
- PostgreSQL 14+

## Quick Start

1. Create the database:
   ```sql
   CREATE DATABASE ubs_db;
   ```

2. Configure `src/main/resources/application.properties` (your file — not overwritten by the project). See `docs/CONFIG_RECOMMENDATIONS.md` and `application.properties.example` for optional suggestions.

   **Default:** `ddl-auto=update` without Flyway (no `spring.flyway.enabled=true`).

   **With DB routines:** add `spring.flyway.enabled=true` and switch to `ddl-auto=validate`.

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Default Seeded Users

| Email | Password | Role |
|-------|----------|------|
| admin@wasac.rw | Password@123 | ROLE_ADMIN |
| finance@wasac.rw | Password@123 | ROLE_FINANCE |
| operator@wasac.rw | Password@123 | ROLE_OPERATOR |
| customer@wasac.rw | Password@123 | ROLE_CUSTOMER |

## Typical Workflow

1. **Login** — `POST /api/auth/login` → copy JWT token
2. **Authorize in Swagger** — click Authorize, enter `Bearer <token>`
3. **Register customer** — `POST /api/customers` (ADMIN/OPERATOR)
4. **Install meter** — `POST /api/meters`
5. **Capture reading** — `POST /api/readings` (OPERATOR) — include `previousReading`
6. **Configure tariff** — `POST /api/tariffs` (ADMIN)
7. **Generate bill** — `POST /api/bills/generate` or `POST /api/bills/generate-batch` (ADMIN)
8. **Approve bill** — `PATCH /api/bills/{id}/approve` (ADMIN/FINANCE)
9. **Record payment** — `POST /api/payments` (ADMIN/FINANCE)
10. **Approve payment** — `PATCH /api/payments/{id}/approve` (ADMIN/FINANCE)

Customers linked to a profile can use `/api/bills/me`, `/api/payments/me`, and `/api/messages/me`.

## Database Migrations (opt-in)

Flyway runs **only** when you set `spring.flyway.enabled=true` in your `application.properties`.

Migration files (when enabled):

- `V1__schema.sql` — tables
- `V2__database_routines.sql` — triggers, stored procedures, cursor
- `V3__message_email_pending.sql` — messages pending email dispatch

Without Flyway, use Hibernate `ddl-auto=update` and apply `db/routines.sql` manually if needed.

## Documentation

- [ERD](docs/ERD.md)
- [Flow Diagram](docs/FLOW_DIAGRAM.md)
- [Assignment](docs/Assigment.txt)

## Email Notifications

DB triggers insert messages with `sent=false`. When `spring.mail.username` is configured, `MessageDispatchScheduler` emails customers every 60 seconds and marks messages as sent.
