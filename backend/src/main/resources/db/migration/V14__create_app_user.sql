create table if not exists app_user (
    id bigserial primary key,
    username varchar(100) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp
);

create index if not exists idx_app_user_username
    on app_user(username);

create index if not exists idx_app_user_role
    on app_user(role);

create index if not exists idx_app_user_active
    on app_user(active);
