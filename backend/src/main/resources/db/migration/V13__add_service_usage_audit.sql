create table if not exists service_usage_audit (
    id bigserial primary key,
    service_id bigint,
    service_name varchar(255),
    usage_id bigint,
    action varchar(64) not null,
    old_subject varchar(255),
    new_subject varchar(255),
    old_version integer,
    new_version integer,
    old_role varchar(32),
    new_role varchar(32),
    old_active boolean,
    new_active boolean,
    old_service_active boolean,
    new_service_active boolean,
    changed_by varchar(255),
    reason text,
    created_at timestamp with time zone not null default current_timestamp
);

create index if not exists idx_service_usage_audit_service_id
    on service_usage_audit(service_id);

create index if not exists idx_service_usage_audit_usage_id
    on service_usage_audit(usage_id);

create index if not exists idx_service_usage_audit_created_at
    on service_usage_audit(created_at);

create index if not exists idx_service_usage_audit_action
    on service_usage_audit(action);
