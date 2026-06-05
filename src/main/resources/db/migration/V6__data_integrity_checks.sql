DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'meter_readings') THEN
        ALTER TABLE meter_readings
            ADD CONSTRAINT ck_meter_readings_non_negative
                CHECK (previous_reading >= 0 AND current_reading >= 0 AND consumption >= 0);
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'meter_readings') THEN
        ALTER TABLE meter_readings
            ADD CONSTRAINT ck_meter_readings_period
                CHECK (reading_month BETWEEN 1 AND 12);
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'tariffs') THEN
        ALTER TABLE tariffs
            ADD CONSTRAINT ck_tariffs_non_negative
                CHECK (
                    COALESCE(unit_price, 0) >= 0
                    AND service_charge >= 0
                    AND vat_percentage >= 0
                    AND late_payment_penalty_percentage >= 0
                );
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'tariff_tiers') THEN
        ALTER TABLE tariff_tiers
            ADD CONSTRAINT ck_tariff_tiers_non_negative
                CHECK (from_unit >= 0 AND to_unit > from_unit AND price_per_unit >= 0);
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'bills') THEN
        ALTER TABLE bills
            ADD CONSTRAINT ck_bills_period
                CHECK (billing_month BETWEEN 1 AND 12);
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'bills') THEN
        ALTER TABLE bills
            ADD CONSTRAINT ck_bills_amounts_non_negative
                CHECK (
                    consumption >= 0
                    AND total_amount >= 0
                    AND paid_amount >= 0
                    AND outstanding_balance >= 0
                );
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'payments') THEN
        ALTER TABLE payments
            ADD CONSTRAINT ck_payments_amount_non_negative
                CHECK (amount_paid > 0);
    END IF;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
