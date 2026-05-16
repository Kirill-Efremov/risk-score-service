ALTER TABLE schema_analysis
    ADD COLUMN promotion_attempted BOOLEAN,
    ADD COLUMN registered BOOLEAN,
    ADD COLUMN registration_status VARCHAR(64),
    ADD COLUMN registered_version INTEGER,
    ADD COLUMN schema_registry_id INTEGER;
