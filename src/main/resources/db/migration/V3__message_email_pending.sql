-- Mark DB-triggered messages as pending until JavaMailSender dispatches them

CREATE OR REPLACE FUNCTION trg_bill_generated_notify()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_period TEXT;
BEGIN
    v_period := fn_format_billing_period(NEW.billing_month, NEW.billing_year);
    INSERT INTO messages (id, customer_id, content, message_type, sent, created_at)
    VALUES (
        gen_random_uuid(),
        NEW.customer_id,
        fn_build_bill_notification(NEW.customer_id, v_period, NEW.total_amount),
        'BILL_GENERATED',
        FALSE,
        NOW()
    );
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION trg_bill_paid_notify()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_period TEXT;
BEGIN
    IF NEW.status = 'PAID' AND (OLD.status IS NULL OR OLD.status IS DISTINCT FROM 'PAID') THEN
        v_period := fn_format_billing_period(NEW.billing_month, NEW.billing_year);
        INSERT INTO messages (id, customer_id, content, message_type, sent, created_at)
        VALUES (
            gen_random_uuid(),
            NEW.customer_id,
            fn_build_bill_notification(NEW.customer_id, v_period, NEW.total_amount),
            'PAYMENT_COMPLETE',
            FALSE,
            NOW()
        );
    END IF;
    RETURN NEW;
END;
$$;
