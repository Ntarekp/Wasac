# Entity Relationship Diagram — Utility Billing System

**Assignment requirement:** Design an ERD before implementation.

**DBML (dbdiagram.io):** Paste [`docs/ERD.dbml`](ERD.dbml) into [https://dbdiagram.io](https://dbdiagram.io) for an interactive diagram with sample records.

## Diagram

```mermaid
erDiagram
    USERS ||--o| CUSTOMERS : "linked (ROLE_CUSTOMER)"
    CUSTOMERS ||--{ METERS : owns
    METERS ||--{ METER_READINGS : has
    CUSTOMERS ||--{ BILLS : receives
    METERS ||--{ BILLS : billed_on
    METER_READINGS ||--o| BILLS : generates
    TARIFFS ||--{ BILLS : priced_with
    TARIFFS ||--{ TARIFF_TIERS : contains
    BILLS ||--{ PAYMENTS : paid_by
    CUSTOMERS ||--{ MESSAGES : notified

    USERS {
        uuid id PK
        string full_names
        string email UK
        string phone_number
        string password
        enum role
        enum status
        uuid customer_id FK
        datetime created_at
    }

    CUSTOMERS {
        uuid id PK
        string full_names
        string national_id UK
        string email UK
        string phone_number
        string address
        enum status
    }

    METERS {
        uuid id PK
        string meter_number UK
        enum meter_type
        date installation_date
        enum status
        uuid customer_id FK
    }

    METER_READINGS {
        uuid id PK
        uuid meter_id FK
        double previous_reading
        double current_reading
        date reading_date
        int reading_month
        int reading_year
        double consumption
    }

    TARIFFS {
        uuid id PK
        string name
        int version
        enum meter_type
        enum tariff_type
        double unit_price
        double service_charge
        double vat_percentage
        double late_payment_penalty_percentage
        date effective_from
        boolean active
    }

    TARIFF_TIERS {
        uuid id PK
        uuid tariff_id FK
        double from_unit
        double to_unit
        double price_per_unit
    }

    BILLS {
        uuid id PK
        string bill_reference UK
        uuid customer_id FK
        uuid meter_id FK
        uuid reading_id FK
        uuid tariff_id FK
        int billing_month
        int billing_year
        double total_amount
        double paid_amount
        double outstanding_balance
        enum status
        date due_date
        boolean penalty_applied
    }

    PAYMENTS {
        uuid id PK
        uuid bill_id FK
        double amount_paid
        enum payment_method
        date payment_date
        enum status
    }

    MESSAGES {
        uuid id PK
        uuid customer_id FK
        string content
        string message_type
        boolean sent
        datetime created_at
    }
```

## Key Relationships

| Relationship | Cardinality | Description |
|--------------|-------------|-------------|
| User → Customer | 0..1 | Customer-role users are linked to a customer profile for scoped access |
| Customer → Meter | 1..* | Each customer may have multiple utility meters |
| Meter → MeterReading | 1..* | One reading per meter per month/year (unique constraint) |
| Bill → Tariff | *..1 | Bill stores the tariff version used at generation time |
| Bill → Payment | 1..* | Supports partial and full payments |
| Customer → Message | 1..* | Notifications auto-inserted via DB triggers |

## Database Routines (Task 6)

| Routine | Type | Event |
|---------|------|-------|
| `trg_bill_generated_notify` | Trigger | AFTER INSERT on `bills` |
| `trg_bill_paid_notify` | Trigger | AFTER UPDATE OF `status` on `bills` when status → PAID |
| `sp_apply_late_penalty` | Stored Procedure | Apply penalty to one bill |
| `sp_apply_all_late_penalties` | Stored Procedure + Cursor | Batch penalty application |
