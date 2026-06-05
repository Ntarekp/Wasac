# Spring Boot Flow Diagram — Utility Billing System

**Assignment requirement:** Design a Spring Boot flow diagram showing system functionality.

## Request Flow (All Protected Endpoints)

```mermaid
flowchart TD
    A[Client Request] --> B{Public endpoint?}
    B -->|/api/auth/** Swagger| C[Process without JWT]
    B -->|Protected| D[JwtAuthenticationFilter]
    D --> E{Valid Bearer token?}
    E -->|No| F[401 Unauthorized]
    E -->|Yes| G{User ACTIVE?}
    G -->|No| F
    G -->|Yes| H[SecurityContext populated]
    H --> I[Controller @PreAuthorize role check]
    I -->|Denied| J[403 Forbidden]
    I -->|Allowed| K{ROLE_CUSTOMER?}
    K -->|Yes| L[SecurityService ownership check]
    L -->|Fail| J
    K -->|No| M[Service Layer]
    L -->|Pass| M
    M --> N[Repository / JPA]
    N --> O[(PostgreSQL)]
    O --> P[DB Triggers fire on bill insert/update]
    P --> Q[Response JSON]
    M --> Q
```

## Billing Lifecycle

```mermaid
flowchart LR
    subgraph Task2[Customer & Meter]
        C1[Register Customer] --> C2[Install Meter]
    end

    subgraph Task3[Meter Reading]
        C2 --> R1[Operator captures reading]
        R1 --> R2{Rules: active meter, unique month, current > previous}
    end

    subgraph Task4[Tariff]
        T1[Admin configures tariff version]
    end

    subgraph Task5[Bill & Payment]
        R2 --> B1[Generate Bill UNPAID]
        B1 --> DB1[(Trigger: insert message)]
        B1 --> B2[Admin/Finance approves APPROVED]
        B2 --> P1[Finance records payment PENDING]
        P1 --> P2[Finance approves payment]
        P2 --> B3[Update bill balance]
        B3 --> DB2[(Trigger: PAID notification)]
    end

    T1 -.-> B1
```

## Role Responsibilities

```mermaid
flowchart TB
    ADMIN[ROLE_ADMIN] --> A1[Manage users]
    ADMIN --> A2[Configure tariffs]
    ADMIN --> A3[Approve bills & payments]

    OPERATOR[ROLE_OPERATOR] --> O1[Register customers]
    OPERATOR --> O2[Install meters]
    OPERATOR --> O3[Capture readings]
    OPERATOR --> O4[Generate bills]

    FINANCE[ROLE_FINANCE] --> F1[Approve bills]
    FINANCE --> F2[Record & approve payments]
    FINANCE --> F3[View overdue bills]

    CUSTOMER[ROLE_CUSTOMER] --> CU1[View own bills]
    CUSTOMER --> CU2[View own payment history]
    CUSTOMER --> CU3[View own notifications]
```

## Component Map

| Layer | Packages |
|-------|----------|
| Controllers | `auth`, `user`, `customer`, `meter`, `reading`, `tariff`, `bill`, `payment`, `penalty`, `message` |
| Services | Same domain packages + `security.SecurityService` |
| Security | `JwtAuthenticationFilter`, `SecurityConfig`, `CustomUserDetailsService` |
| Persistence | Spring Data JPA repositories |
| DB Routines | Flyway `V1__database_routines.sql` |
