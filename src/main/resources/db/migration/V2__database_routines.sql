-- Task 6: Database-level routines (triggers, stored procedures, cursor)

CREATE OR REPLACE FUNCTION fn_format_billing_period(p_month INT, p_year INT)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN TRIM(TO_CHAR(TO_DATE(p_year || '-' || p_month || '-01', 'YYYY-MM-DD'), 'Month')) || '/' || p_year;
END;
$$;

CREATE OR REPLACE FUNCTION fn_build_bill_notification(
    p_customer_id UUID,
    p_month_year TEXT,
    p_amount DOUBLE PRECISION
)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
DECLARE
    v_name TEXT;
BEGIN
    SELECT full_names INTO v_name FROM customers WHERE id = p_customer_id;
    RETURN 'Dear ' || COALESCE(v_name, 'Customer')
        || ', Your ' || p_month_year
        || ' utility bill of ' || ROUND(p_amount::numeric, 2) || 'FRW has been successfully processed';
END;
$$;

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
        TRUE,
        NOW()
    );
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS bill_generated_notification ON bills;
CREATE TRIGGER bill_generated_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION trg_bill_generated_notify();

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
            TRUE,
            NOW()
        );
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS bill_paid_notification ON bills;
CREATE TRIGGER bill_paid_notification
    AFTER UPDATE OF status ON bills
    FOR EACH ROW
    EXECUTE FUNCTION trg_bill_paid_notify();

CREATE OR REPLACE PROCEDURE sp_apply_late_penalty(
    IN p_bill_id UUID,
    OUT p_penalty_amount DOUBLE PRECISION
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_outstanding DOUBLE PRECISION;
    v_penalty_rate DOUBLE PRECISION;
    v_new_total DOUBLE PRECISION;
    v_new_balance DOUBLE PRECISION;
BEGIN
    p_penalty_amount := 0;

    SELECT b.outstanding_balance, t.late_payment_penalty_percentage
    INTO v_outstanding, v_penalty_rate
    FROM bills b
    JOIN tariffs t ON t.id = b.tariff_id
    WHERE b.id = p_bill_id
      AND b.penalty_applied = FALSE
      AND b.due_date < CURRENT_DATE
      AND b.status IN ('UNPAID', 'PARTIALLY_PAID', 'APPROVED', 'OVERDUE');

    IF NOT FOUND THEN
        RETURN;
    END IF;

    p_penalty_amount := ROUND((v_outstanding * (v_penalty_rate / 100.0))::numeric, 2);
    v_new_total := ROUND((SELECT total_amount FROM bills WHERE id = p_bill_id) + p_penalty_amount, 2);
    v_new_balance := ROUND(v_outstanding + p_penalty_amount, 2);

    UPDATE bills
    SET total_amount = v_new_total,
        outstanding_balance = v_new_balance,
        status = 'OVERDUE',
        penalty_applied = TRUE,
        updated_at = NOW()
    WHERE id = p_bill_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_apply_all_late_penalties()
LANGUAGE plpgsql
AS $$
DECLARE
    v_bill_id UUID;
    v_penalty DOUBLE PRECISION;
    bill_cursor CURSOR FOR
        SELECT id
        FROM bills
        WHERE penalty_applied = FALSE
          AND due_date < CURRENT_DATE
          AND status IN ('UNPAID', 'PARTIALLY_PAID', 'APPROVED');
BEGIN
    OPEN bill_cursor;
    LOOP
        FETCH bill_cursor INTO v_bill_id;
        EXIT WHEN NOT FOUND;
        CALL sp_apply_late_penalty(v_bill_id, v_penalty);
    END LOOP;
    CLOSE bill_cursor;
END;
$$;
