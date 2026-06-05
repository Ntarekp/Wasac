# Finalization Plan — Assignment Completion

## P0 — Critical (Task 6 + stable DB)

| # | Item | Action |
|---|------|--------|
| 1 | DB routines inactive | Enable Flyway, `ddl-auto=none`, `baseline-on-migrate` |
| 2 | Data wiped on restart | Remove `create-drop` |
| 3 | Schema drift | Sync `V1__schema.sql` with entities (user flags, `otps`) |

## P1 — High (business rules + security)

| # | Item | Action |
|---|------|--------|
| 4 | Tariff `effectiveFrom` ignored | `getTariffForBilling(month, year)` in billing |
| 5 | `/api/auth/**` all public | Only login, register, OTP, reset-password public |

## P2 — Medium (assignment alignment)

| # | Item | Action |
|---|------|--------|
| 6 | Penalties in Java only | Call `sp_apply_all_late_penalties()` with Java fallback |
| 7 | Bill generation ADMIN-only | Allow `ROLE_OPERATOR` per flow diagram |
| 8 | Messages without triggers | Java fallback in `BillService` when `app.messaging.java-fallback=true` |

## P3 — Low (documentation)

| # | Item | Action |
|---|------|--------|
| 9 | README outdated | OTP login, Gmail seeds, Flyway mode B |
| 10 | Flow diagram | Note operator bill generation |

## Out of scope

- Prepaid electricity (scenario narrative only)
- Full meter PUT/DELETE endpoints
