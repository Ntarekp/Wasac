-- Run in pgAdmin when Flyway/JPA schema is out of sync (e.g. after ddl-auto=create-drop).
-- WARNING: deletes ALL data in ubs_db public schema.

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- Then restart the Spring Boot app — Flyway will apply V1 through V4 and DataSeeder will run.
