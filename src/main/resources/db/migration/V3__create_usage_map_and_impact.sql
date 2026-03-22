create table if not exists service (
    id bigserial primary key,
    name varchar(255) not null unique,
    critical boolean not null default false,
    created_at timestamp with time zone not null default current_timestamp
);

create table if not exists service_schema_usage (
    id bigserial primary key,
    service_id bigint not null references service(id),
    subject varchar(255) not null,
    version integer null,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp
);

create index if not exists idx_service_schema_usage_subject
    on service_schema_usage(subject);

create index if not exists idx_service_schema_usage_subject_version
    on service_schema_usage(subject, version);

alter table schema_analysis
    add column if not exists impact_json text null;
