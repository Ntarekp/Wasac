-- Trigger to automatically update bill status and outstanding balance on payment approval
CREATE OR REPLACE FUNCTION trg_payment_approved_update_bill()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_outstanding DOUBLE PRECISION;
    v_paid DOUBLE PRECISION;
    v_new_paid DOUBLE PRECISION;
    v_new_balance DOUBLE PRECISION;
    v_new_status VARCHAR(50);
BEGIN
    -- Only run when status changes to 'APPROVED'
    IF NEW.status = 'APPROVED' AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM 'APPROVED') THEN
        -- Get current bill values
        SELECT paid_amount, outstanding_balance
        INTO v_paid, v_outstanding
        FROM bills
        WHERE id = NEW.bill_id;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Bill not found for payment';
        END IF;

        v_new_paid := ROUND((v_paid + NEW.amount_paid)::numeric, 2);
        v_new_balance := ROUND((v_outstanding - NEW.amount_paid)::numeric, 2);

        IF v_new_balance <= 0 THEN
            v_new_status := 'PAID';
            v_new_balance := 0;
        ELSE
            v_new_status := 'PARTIALLY_PAID';
        END IF;

        UPDATE bills
        SET paid_amount = v_new_paid,
            outstanding_balance = v_new_balance,
            status = v_new_status,
            updated_at = NOW()
        WHERE id = NEW.bill_id;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS payment_approved_update_bill ON payments;
CREATE TRIGGER payment_approved_update_bill
    AFTER INSERT OR UPDATE OF status ON payments
    FOR EACH ROW
    EXECUTE FUNCTION trg_payment_approved_update_bill();
