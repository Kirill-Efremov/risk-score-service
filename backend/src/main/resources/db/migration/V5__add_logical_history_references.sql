alter table schema_analysis
    add column if not exists subject_name varchar(255);

alter table schema_analysis
    add column if not exists old_version integer;

alter table schema_analysis
    add column if not exists new_version integer;

alter table schema_analysis
    add column if not exists source_type varchar(64);

alter table schema_analysis
    add column if not exists external_schema_id varchar(255);

update schema_analysis sa
set subject_name = ss.name
from schema_subject ss
where sa.subject_name is null
  and sa.subject_id = ss.id;

update schema_analysis sa
set old_version = sv.version
from schema_version sv
where sa.old_version is null
  and sa.old_version_id = sv.id;

update schema_analysis sa
set new_version = sv.version
from schema_version sv
where sa.new_version is null
  and sa.new_version_id = sv.id;

update schema_analysis
set source_type = 'LOCAL'
where source_type is null;

update schema_analysis sa
set external_schema_id = sv.external_schema_id
from schema_version sv
where sa.external_schema_id is null
  and sa.new_version_id = sv.id;

create index if not exists idx_schema_analysis_subject_name_created_at
    on schema_analysis(subject_name, created_at desc);
