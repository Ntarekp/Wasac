-- Core schema managed by Flyway (replaces Hibernate ddl-auto=update)

CREATE TABLE customers (
    id          UUID PRIMARY KEY,
    full_names  VARCHAR(255) NOT NULL,
    national_id VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE users (
    id           UUID PRIMARY KEY,
    full_names   VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    customer_id  UUID REFERENCES customers(id),
    role         VARCHAR(50)  NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP
);

CREATE TABLE meters (
    id                UUID PRIMARY KEY,
    meter_number      VARCHAR(255) NOT NULL UNIQUE,
    meter_type        VARCHAR(50)  NOT NULL,
    installation_date DATE         NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    customer_id       UUID         NOT NULL REFERENCES customers(id),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP
);

CREATE TABLE meter_readings (
    id                UUID PRIMARY KEY,
    meter_id          UUID           NOT NULL REFERENCES meters(id),
    previous_reading  DOUBLE PRECISION NOT NULL,
    current_reading   DOUBLE PRECISION NOT NULL,
    reading_date      DATE           NOT NULL,
    reading_month     INTEGER        NOT NULL,
    reading_year      INTEGER        NOT NULL,
    consumption       DOUBLE PRECISION NOT NULL,
    created_at        TIMESTAMP      NOT NULL,
    CONSTRAINT uq_reading_meter_period UNIQUE (meter_id, reading_month, reading_year)
);

CREATE TABLE tariffs (
    id                            UUID PRIMARY KEY,
    name                          VARCHAR(255) NOT NULL,
    version                       INTEGER      NOT NULL,
    meter_type                    VARCHAR(50)  NOT NULL,
    tariff_type                   VARCHAR(50)  NOT NULL,
    unit_price                    DOUBLE PRECISION,
    service_charge                DOUBLE PRECISION NOT NULL,
    vat_percentage                DOUBLE PRECISION NOT NULL,
    late_payment_penalty_percentage DOUBLE PRECISION NOT NULL,
    effective_from                DATE         NOT NULL,
    effective_to                  DATE,
    active                        BOOLEAN,
    created_at                    TIMESTAMP    NOT NULL
);

CREATE TABLE tariff_tiers (
    id             UUID PRIMARY KEY,
    tariff_id      UUID           NOT NULL REFERENCES tariffs(id),
    from_unit      DOUBLE PRECISION NOT NULL,
    to_unit        DOUBLE PRECISION NOT NULL,
    price_per_unit DOUBLE PRECISION NOT NULL
);

CREATE TABLE bills (
    id                  UUID PRIMARY KEY,
    bill_reference      VARCHAR(255) NOT NULL UNIQUE,
    customer_id         UUID         NOT NULL REFERENCES customers(id),
    meter_id            UUID         NOT NULL REFERENCES meters(id),
    reading_id          UUID         NOT NULL REFERENCES meter_readings(id),
    tariff_id           UUID         NOT NULL REFERENCES tariffs(id),
    billing_month       INTEGER      NOT NULL,
    billing_year        INTEGER      NOT NULL,
    consumption         DOUBLE PRECISION NOT NULL,
    total_amount        DOUBLE PRECISION NOT NULL,
    paid_amount         DOUBLE PRECISION NOT NULL,
    outstanding_balance DOUBLE PRECISION NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    due_date            DATE,
    penalty_applied     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP
);

CREATE TABLE payments (
    id                    UUID PRIMARY KEY,
    bill_id               UUID         NOT NULL REFERENCES bills(id),
    amount_paid           DOUBLE PRECISION NOT NULL,
    payment_method        VARCHAR(50)  NOT NULL,
    payment_date          DATE         NOT NULL,
    transaction_reference VARCHAR(255),
    status                VARCHAR(50)  NOT NULL,
    created_at            TIMESTAMP    NOT NULL
);

CREATE TABLE messages (
    id           UUID PRIMARY KEY,
    customer_id  UUID         NOT NULL REFERENCES customers(id),
    content      VARCHAR(1000) NOT NULL,
    message_type VARCHAR(255) NOT NULL,
    sent         BOOLEAN      NOT NULL,
    created_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_bills_customer_id ON bills(customer_id);
CREATE INDEX idx_bills_meter_period ON bills(meter_id, billing_month, billing_year);
CREATE INDEX idx_meter_readings_period ON meter_readings(reading_month, reading_year);
CREATE INDEX idx_payments_bill_id ON payments(bill_id);
