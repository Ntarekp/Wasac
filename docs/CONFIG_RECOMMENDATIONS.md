# Configuration Recommendations

Your `application.properties` is **yours to own**. This document lists suggested changes you can accept or reject.

## What caused your startup error

The surface error was `Connection is closed`, but the **real PostgreSQL error** was:

```
ERROR: cannot alter type of a column used in a trigger definition
Detail: trigger bill_paid_notification on table bills depends on column "status"
```

Hibernate (`ddl-auto=update`) tried to run:

```sql
ALTER TABLE bills ALTER COLUMN status SET DATA TYPE varchar(255);
```

Your database already has **DB triggers** on `bills.status` (from `db/routines.sql` or earlier Flyway runs). PostgreSQL blocks that `ALTER`, which breaks the JDBC connection and cascades into the `Connection is closed` message.

**Fix applied in `application.properties`:**

```properties
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=none
spring.devtools.restart.enabled=false
```

- `ddl-auto=none` — Hibernate no longer alters tables that have triggers
- `flyway.enabled=false` — Flyway does not run alongside Hibernate
- `devtools.restart.enabled=false` — avoids DevTools restarting mid-startup

If you ever need Hibernate to alter schema again, run `db/manual-fix-hibernate-trigger-conflict.sql` first (drops triggers), update schema, then re-apply `V2__database_routines.sql`.

---

## Your current setup (MODE A — no action required)

```properties
spring.jpa.hibernate.ddl-auto=update
```

- Hibernate creates/updates tables
- DB triggers/routines from `db/migration/` are **not** applied automatically
- To add routines manually: run `src/main/resources/db/routines.sql` or migration SQL in pgAdmin

---

## Optional upgrade (MODE B — Flyway + routines)

Add these lines **yourself** if you want automated schema + triggers:

```properties
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.baseline-on-migrate=true
```

**Important:** Change `ddl-auto` from `update` to `validate` when enabling Flyway.

If the database already has tables from Hibernate, `baseline-on-migrate` lets Flyway skip V1 on first run.

---

---

## Security (recommended for production)

Move secrets to environment variables instead of plain text in properties:

```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
spring.mail.password=${MAIL_PASSWORD}
```

See `application.properties.example` for the full list.
