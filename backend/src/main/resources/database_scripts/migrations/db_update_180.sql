/* ==========================================================
   CREATE MINIMAL TABLES
   ========================================================== */

create table if not exists project (
	project_id uuid         not null default uuid(),
	code       varchar(128) not null,
	constraint pk_project primary key (project_id),
	constraint uq_project_code unique (code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

insert ignore into project (project_id, code)
values (UUID(), 'TEST');

create table if not exists scope_model (
	scope_model_id uuid         not null default uuid(),
	project_id     uuid         not null,
	code           varchar(128) not null,
	constraint pk_scope_model primary key (project_id, scope_model_id),
	constraint uq_scope_model_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model (
	event_model_id uuid         not null default uuid(),
	project_id     uuid         not null,
	code           varchar(128) not null,
	scope_model_id uuid         null,
	constraint pk_event_model primary key (project_id, event_model_id),
	constraint uq_event_model_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists dataset_model (
	dataset_model_id uuid         not null default uuid(),
	project_id       uuid         not null,
	code             varchar(128) not null,
	constraint pk_dataset_model primary key (project_id, dataset_model_id),
	constraint uq_dataset_model_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists field_model (
	field_model_id   uuid         not null default uuid(),
	project_id       uuid         not null,
	dataset_model_id uuid         not null,
	code             varchar(128) not null,
	constraint pk_field_model primary key (project_id, field_model_id),
	constraint uq_field_model_code unique (project_id, dataset_model_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_model (
	form_model_id uuid         not null default uuid(),
	project_id    uuid         not null,
	code          varchar(128) not null,
	constraint pk_form_model primary key (project_id, form_model_id),
	constraint uq_form_model_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists workflow (
	workflow_id uuid         not null default uuid(),
	project_id  uuid         not null,
	code        varchar(128) not null,
	constraint pk_workflow primary key (project_id, workflow_id),
	constraint uq_workflow_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists workflow_state (
	workflow_state_id uuid         not null default uuid(),
	project_id        uuid         not null,
	workflow_id       uuid         not null,
	code              varchar(128) not null,
	constraint pk_workflow_state primary key (project_id, workflow_state_id),
	constraint uq_workflow_state_code unique (project_id, workflow_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists workflow_action (
	workflow_action_id uuid         not null default uuid(),
	project_id         uuid         not null,
	workflow_id        uuid         not null,
	code               varchar(128) not null,
	constraint pk_workflow_action primary key (project_id, workflow_action_id),
	constraint uq_workflow_action_code unique (project_id, workflow_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists validator (
	validator_id uuid         not null default uuid(),
	project_id   uuid         not null,
	code         varchar(128) not null,
	constraint pk_validator primary key (project_id, validator_id),
	constraint uq_validator_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table payment_plan (
	payment_plan_id uuid         not null default uuid(),
	project_id      uuid         not null,
	code            varchar(128) not null,
	constraint pk_payment_plan primary key (project_id, payment_plan_id),
	constraint uq_payment_plan_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table payment_step (
	payment_step_id uuid         not null default uuid(),
	project_id      uuid         not null,
	payment_plan_id uuid         not null,
	code            varchar(128) not null,
	constraint pk_payment_step primary key (project_id, payment_plan_id, payment_step_id),
	constraint uq_payment_step_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table resource_category (
	category_id uuid         not null default uuid(),
	project_id  uuid         not null,
	code        varchar(128) not null,
	constraint pk_resource_category primary key (project_id, category_id),
	constraint uq_resource_category_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile (
	profile_id uuid         not null default uuid(),
	project_id uuid         not null,
	code       varchar(128) not null,
	constraint pk_profile primary key (project_id, profile_id),
	constraint uq_profile_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;
