# Assignment Implementation Status

Last finalized: 2026-06-05

## Scorecard

| Task | Status |
|------|--------|
| Task 1 — User mgmt & JWT | **Complete** |
| Task 2 — Customer & meter | **Complete** |
| Task 3 — Meter readings | **Complete** |
| Task 4 — Tariffs/VAT/penalties | **Complete** (`effectiveFrom` enforced) |
| Task 5 — Payments | **Complete** |
| Task 6 — DB routines & messaging | **Complete** (Flyway V2–V3 + email scheduler) |
| ERD / Flow / Swagger | **Complete** |
| Prepaid electricity | **N/A** (scenario only) |

## Runtime configuration

```properties
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
app.messaging.java-fallback=false
```

## On next startup

Flyway will apply `V4__otp_and_user_flags.sql` if not yet applied (DB already has V1–V3).

Restart: `./mvnw spring-boot:run`
