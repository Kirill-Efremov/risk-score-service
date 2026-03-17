create table if not exists schema_analysis (
    id bigserial primary key,
    subject_id bigint null references schema_subject(id),
    old_version_id bigint null references schema_version(id),
    new_version_id bigint null references schema_version(id),
    compatibility_mode varchar(32) not null,
    formal_compatible boolean not null,
    issues_json text not null,
    diff_json text null,
    risk_score integer not null,
    risk_level varchar(32) not null,
    decision varchar(32) not null,
    recommendations_json text not null,
    created_at timestamp with time zone not null default current_timestamp,
    created_by varchar(255) null
);

create index if not exists idx_schema_analysis_subject_id_created_at
    on schema_analysis(subject_id, created_at desc);
