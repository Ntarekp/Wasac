# Utility Billing System (ubsystem)

Spring Boot backend for WASAC/REG utility billing — customers, meters, readings, tariffs, bills, payments, DB routines, and automated notifications.

## Prerequisites

- Java 25+
- Maven 3.9+
- PostgreSQL 14+

## Quick Start

1. Create the database:
   ```sql
   CREATE DATABASE ubs_db;
   ```

2. Configure `src/main/resources/application.properties` (credentials stay in your local file).

   **Assignment-complete mode (default):**
   ```properties
   spring.jpa.hibernate.ddl-auto=none
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — **Authentication** is the first section.

## Default Seeded Users

Welcome emails with temporary passwords are sent on first seed.

| Email | Role |
|-------|------|
| kpntare@gmail.com | ROLE_ADMIN |
| benmu91@gmail.com | ROLE_FINANCE |
| Cabledie@gmail.com | ROLE_OPERATOR |
| devroom210@gmail.com | ROLE_CUSTOMER |

## Login (OTP required every time)

1. `POST /api/auth/login` — validate password → OTP emailed
2. `POST /api/auth/otp/verify` — `{ "email", "code", "purpose": "LOGIN" }` → JWT
3. Swagger **Authorize** — paste token (no `Bearer` prefix in the box)
4. If `mustChangePassword: true` → `POST /api/auth/change-password` before other APIs

## Typical Workflow

1. **Login** — login → OTP verify → Authorize in Swagger
2. **Register customer** — `POST /api/customers` (ADMIN/OPERATOR)
3. **Install meter** — `POST /api/meters`
4. **Capture reading** — `POST /api/readings` (OPERATOR)
5. **Configure tariff** — `POST /api/tariffs` (ADMIN) — set `effectiveFrom` for future cycles
6. **Generate bill** — `POST /api/bills/generate` or `/generate-batch` (ADMIN/OPERATOR)
7. **Approve bill** — `PATCH /api/bills/{id}/approve` (ADMIN/FINANCE)
8. **Record payment** — `POST /api/payments` (ADMIN/FINANCE)
9. **Approve payment** — `PATCH /api/payments/{id}/approve` (ADMIN/FINANCE)

Customers use `/api/bills/me`, `/api/payments/me`, `/api/messages/me`.

## Database (Flyway)

| Migration | Purpose |
|-----------|---------|
| `V1__schema.sql` | Tables (users, customers, bills, messages, otps, …) |
| `V2__database_routines.sql` | Triggers, stored procedures, cursor (Task 6) |
| `V3__message_email_pending.sql` | Messages `sent=false` for email dispatch |
| `V4__otp_and_user_flags.sql` | Upgrade path for existing DBs |

**Task 6:** Bill insert → trigger inserts message; bill PAID → trigger notifies customer. `MessageDispatchScheduler` emails customers when mail is configured.

**Fallback:** Set `app.messaging.java-fallback=true` only if Flyway/triggers are disabled.

**Penalties:** `POST /api/penalties/apply` calls `sp_apply_all_late_penalties()` (Java fallback if SP missing).

## Documentation

- [ERD](docs/ERD.md)
- [Flow Diagram](docs/FLOW_DIAGRAM.md)
- [Finalization Plan](docs/FINALIZATION_PLAN.md)
- [Assignment](docs/Assigment.txt)
- [Config recommendations](docs/CONFIG_RECOMMENDATIONS.md)

## Email

- **Auth:** OTP, welcome credentials, password changed
- **Billing:** Bill generated / payment complete (via DB trigger → scheduler → `EmailService`)

Configure `spring.mail.username` as a plain email address (not markdown).
