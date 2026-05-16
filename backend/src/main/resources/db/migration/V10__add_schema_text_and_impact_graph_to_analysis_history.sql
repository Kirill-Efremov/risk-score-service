ALTER TABLE schema_analysis
    ADD COLUMN IF NOT EXISTS impact_graph_json TEXT,
    ADD COLUMN IF NOT EXISTS old_schema_text TEXT,
    ADD COLUMN IF NOT EXISTS new_schema_text TEXT;
