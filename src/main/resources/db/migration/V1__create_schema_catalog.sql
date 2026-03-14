create table if not exists schema_subject (
    id bigserial primary key,
    name varchar(255) not null unique,
    schema_type varchar(32) not null,
    default_compatibility_mode varchar(32) not null,
    description text null,
    created_at timestamp with time zone not null default current_timestamp
);

create table if not exists schema_version (
    id bigserial primary key,
    subject_id bigint not null references schema_subject(id),
    version integer not null,
    schema_text text not null,
    schema_hash varchar(64) not null,
    status varchar(32) not null,
    source_type varchar(32) not null,
    external_schema_id varchar(255) null,
    created_at timestamp with time zone not null default current_timestamp,
    constraint uq_schema_version_subject_version unique (subject_id, version)
);

create index if not exists idx_schema_version_subject_id on schema_version(subject_id);
create index if not exists idx_schema_version_subject_id_version on schema_version(subject_id, version desc);
