-- Run in pgAdmin when Hibernate ddl-auto=update fails with:
-- "cannot alter type of a column used in a trigger definition"
--
-- After running this script, either:
--   A) Keep spring.jpa.hibernate.ddl-auto=none (recommended with triggers), OR
--   B) Set ddl-auto=update, restart app once, then re-apply db/migration/V2__database_routines.sql

DROP TRIGGER IF EXISTS bill_paid_notification ON bills;
DROP TRIGGER IF EXISTS bill_generated_notification ON bills;

-- Optional: only if Hibernate still needs wider enum columns
-- ALTER TABLE bills ALTER COLUMN status TYPE varchar(255);
