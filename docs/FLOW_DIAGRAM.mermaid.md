# Mermaid scripts — copy each block into https://mermaid.live

---

## 1. Spring Boot System Architecture (main submission diagram)

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        SW[Swagger UI]
        PM[Postman]
    end

    subgraph SpringBoot["Spring Boot Application"]
        subgraph Security["Security Layer"]
            SC[SecurityConfig]
            JWT[JwtAuthenticationFilter]
            MCP[MustChangePasswordFilter]
            SS[SecurityService]
        end

        subgraph Web["Controller Layer"]
            AUTH[AuthController]
            DOM[Domain Controllers<br/>customer · meter · reading · tariff<br/>bill · payment · message · user]
        end

        subgraph Business["Service Layer"]
            SVC[Domain Services + OtpService<br/>AuthAccountEmailService]
        end

        subgraph Data["Persistence Layer"]
            REPO[Spring Data JPA Repositories]
        end

        subgraph Jobs["Background Jobs"]
            SCH[MessageDispatchScheduler]
            PEN[PenaltyService Scheduler]
        end
    end

    subgraph External["External"]
        MAIL[(SMTP Email)]
    end

    subgraph Database["PostgreSQL"]
        PG[(Tables)]
        TRG[Triggers & Stored Procedures<br/>bill notify · payment notify · penalties]
    end

    SW --> AUTH
    PM --> DOM
    AUTH --> JWT
    DOM --> JWT
    JWT --> MCP
    MCP --> SS
    SS --> DOM
    AUTH --> SVC
    DOM --> SVC
    SVC --> REPO
    REPO --> PG
    PG --> TRG
    TRG --> PG
    SCH --> MAIL
    SVC --> MAIL
    PEN --> PG
```

---

## 2. Authentication Flow (JWT + OTP)

```mermaid
flowchart TD
    START([User opens app]) --> LOGIN[POST /api/auth/login<br/>email + password]
    LOGIN --> VAL{Credentials valid?}
    VAL -->|No| E401[401 Unauthorized]
    VAL -->|Yes| OTP[OtpService sends 6-digit code by email]
    OTP --> VERIFY[POST /api/auth/otp/verify<br/>purpose: LOGIN]
    VERIFY --> VOK{OTP valid?}
    VOK -->|No| E400[400 Bad Request]
    VOK -->|Yes| JWT[Return JWT token]
    JWT --> MCP{mustChangePassword?}
    MCP -->|Yes| CPW[POST /api/auth/change-password]
    CPW --> API[Access protected APIs]
    MCP -->|No| API

    API --> HDR[Authorization: Bearer token]
    HDR --> FILTER[JwtAuthenticationFilter]
    FILTER --> ROLE[@PreAuthorize role check]
    ROLE --> OK([200 Response])
```

---

## 3. Billing Lifecycle (Tasks 2–6)

```mermaid
flowchart TD
    subgraph T2["Task 2 — Customer & Meter"]
        A1[Admin/Operator: Register Customer] --> A2[Install Meter<br/>WATER or ELECTRICITY]
    end

    subgraph T4["Task 4 — Tariff"]
        T1[Admin: Configure Tariff<br/>flat or tier · VAT · penalty]
    end

    subgraph T3["Task 3 — Meter Reading"]
        A2 --> R1[Operator: Capture Reading]
        R1 --> R2{Business rules<br/>active meter · one/month<br/>current > previous}
        R2 -->|Fail| RX[Reject]
    end

    subgraph T5["Task 5 — Bill & Payment"]
        R2 -->|Pass| B1[Generate Bill — UNPAID]
        B1 --> B2[Admin/Finance: Approve Bill — APPROVED]
        B2 --> P1[Finance: Record Payment — PENDING]
        P1 --> P2[Finance: Approve Payment]
        P2 --> B3{Balance = 0?}
        B3 -->|Yes| PAID[Bill status PAID]
        B3 -->|No| PART[Bill PARTIALLY_PAID]
    end

    subgraph T6["Task 6 — DB Routines & Messaging"]
        B1 --> M1[(Trigger: insert message<br/>BILL_GENERATED)]
        PAID --> M2[(Trigger: insert message<br/>PAYMENT_COMPLETE)]
        M1 --> E1[Email scheduler sends to customer]
        M2 --> E1
    end

    T1 -.->|effective tariff| B1
```

---

## 4. Role Responsibilities

```mermaid
flowchart LR
    subgraph ADMIN["ROLE_ADMIN"]
        direction TB
        AD1[Manage users]
        AD2[Configure tariffs]
        AD3[Approve bills & payments]
        AD4[Apply penalties]
    end

    subgraph OPERATOR["ROLE_OPERATOR"]
        direction TB
        OP1[Register customers]
        OP2[Install meters]
        OP3[Capture readings]
        OP4[Generate bills]
    end

    subgraph FINANCE["ROLE_FINANCE"]
        direction TB
        FI1[Approve bills]
        FI2[Record & approve payments]
        FI3[View overdue bills]
    end

    subgraph CUSTOMER["ROLE_CUSTOMER"]
        direction TB
        CU1[View own bills /api/bills/me]
        CU2[View payments /api/payments/me]
        CU3[View messages /api/messages/me]
    end
```
