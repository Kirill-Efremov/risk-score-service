alter table service
    add column if not exists active boolean not null default true;

alter table service
    add column if not exists owner varchar(255);

alter table service
    add column if not exists description text;

alter table service
    add column if not exists updated_at timestamp with time zone;

update service
set active = true
where active is null;
