ALTER TABLE schema_analysis
    ADD COLUMN IF NOT EXISTS governance_decision VARCHAR(64),
    ADD COLUMN IF NOT EXISTS decision_explanation_json TEXT;
