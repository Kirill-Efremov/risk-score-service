alter table service_schema_usage
    add column if not exists updated_at timestamp with time zone;
