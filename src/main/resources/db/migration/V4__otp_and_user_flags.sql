-- Upgrade path for databases created before user flags / otps were in V1

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
        ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS otps (
    id           UUID PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    code_hash    VARCHAR(255) NOT NULL,
    purpose      VARCHAR(50)  NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    used         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL,
    last_sent_at TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_otp_email_purpose ON otps (email, purpose);
