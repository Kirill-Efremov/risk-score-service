alter table service_schema_usage
    add column if not exists status varchar(32);

alter table service_schema_usage
    add column if not exists active_from timestamp with time zone;

alter table service_schema_usage
    add column if not exists active_to timestamp with time zone;

update service_schema_usage
set status = case
    when active then 'ACTIVE'
    else 'DEPRECATED'
end
where status is null;

update service_schema_usage
set active_from = coalesce(active_from, created_at)
where active_from is null;

update service_schema_usage
set active_to = coalesce(active_to, created_at)
where active = false
  and active_to is null;

alter table service_schema_usage
    alter column status set default 'ACTIVE';

alter table service_schema_usage
    alter column status set not null;

alter table service_schema_usage
    alter column active_from set default current_timestamp;

alter table service_schema_usage
    alter column active_from set not null;

create index if not exists idx_service_schema_usage_status
    on service_schema_usage(status);
