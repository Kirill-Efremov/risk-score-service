ALTER TABLE schema_analysis
    ADD COLUMN IF NOT EXISTS structured_recommendations_json TEXT;
