CREATE TABLE schema_promotion_approval (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    schema_type VARCHAR(32) NOT NULL,
    compatibility_mode VARCHAR(32),
    old_version INTEGER,
    new_schema_text TEXT NOT NULL,
    analysis_id BIGINT,
    formal_compatible BOOLEAN NOT NULL,
    governance_decision VARCHAR(64) NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_by VARCHAR(255),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    admin_comment TEXT,
    registered_version INTEGER,
    schema_registry_id INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_schema_approval_subject ON schema_promotion_approval(subject);
CREATE INDEX idx_schema_approval_status ON schema_promotion_approval(status);
CREATE INDEX idx_schema_approval_requested_by ON schema_promotion_approval(requested_by);
CREATE INDEX idx_schema_approval_requested_at ON schema_promotion_approval(requested_at);
CREATE INDEX idx_schema_approval_analysis_id ON schema_promotion_approval(analysis_id);
