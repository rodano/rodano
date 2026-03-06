/* ==========================================================
   ADD REMAINING COLUMNS AND TABLES
   ========================================================== */

alter table project
	add column shortname                  json                                  null after code,
	add column longname                   json                                  null,
	add column description                json                                  null,
	add column url                        varchar(512)                          null,
	add column email                      varchar(255)                          null,
	add column color                      varchar(9)                            null,
	add column introduction_text          mediumtext                            null,
	add column smtp_tls                   boolean                               not null default false,
	add column password_strong            boolean                               not null default false,
	add column password_length            int                                   null,
	add column password_validity_duration int                                   null,
	add column password_unique            boolean                               not null default false,
	add column epro_enabled               boolean                               not null default false,
	add column epro_profile_id            uuid                                  null,
	add column client_name                varchar(255)                          null,
	add column client_email               varchar(255)                          null,
	add column protocol_no                varchar(64)                           null,
	add column version_number             varchar(32)                           null,
	add column version_date               date                                  null,
	add column status                     enum ('ACTIVE', 'CLOSED', 'ARCHIVED') null     default null,
	add column created                    datetime(3)                           not null default current_timestamp(3),
	add column active_config_version_fk   bigint(20)                            null;

insert into project (project_id,
					 code,
					 shortname,
					 longname,
					 description,
					 smtp_tls,
					 password_strong,
					 password_unique,
					 epro_enabled,
					 status,
					 created)
values (UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')),
		'SYSTEM',
		'{
          "en": "System Administration"
        }',
		'{
          "en": "System Administration Project"
        }',
		'{
          "en": "Internal system project for administrative operations"
        }',
		FALSE,
		FALSE,
		FALSE,
		FALSE,
		'ACTIVE',
		NOW())
on duplicate key update code = code;

create table if not exists project_audit (
	pk              bigint(20)                            not null auto_increment,
	project_id      uuid                                  not null,
	audit_action_fk bigint(20)                            not null,
	audit_datetime  datetime(3)                           not null,
	audit_actor     varchar(200)                          not null,
	audit_user_fk   bigint(20)                            null,
	audit_robot_fk  bigint(20)                            null,
	audit_context   text                                  not null,
	audit_object_fk bigint(20)                            null,
	code            varchar(128)                          not null,
	status          enum ('ACTIVE', 'CLOSED', 'ARCHIVED') not null,
	shortname       json                                  null,
	created         datetime(3)                           not null,
	constraint pk_project_audit primary key (pk)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists project_language (
	project_id uuid       not null,
	language   varchar(8) not null,
	is_default boolean    not null default false,
	constraint pk_project_language primary key (project_id, language)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists project_rule_tag (
	project_id uuid        not null,
	tag        varchar(64) not null,
	constraint pk_project_rule_tag primary key (project_id, tag)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists project_config_version (
	pk              bigint(20)                              not null auto_increment,
	project_id      uuid                                    not null,
	version_number  int                                     not null,
	status          enum ('DRAFT', 'PUBLISHED', 'ARCHIVED') not null,
	created_by      bigint(20)                              null,
	created_at      datetime(3)                             null,
	published_at    datetime(3)                             null,
	published_by    bigint(20)                              null,
	config_snapshot longtext                                not null,
	change_summary  text                                    null,
	constraint pk_project_config_version primary key (pk),
	constraint u_project_version unique (project_id, version_number)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table scope_model
	add column shortname          json         null after code,
	add column longname           json         null,
	add column description        json         null,
	add column plural_shortname   json         null,
	add column virtual            boolean      not null default false,
	add column expected_number    int          null,
	add column max_number         int          null,
	add column scope_format       varchar(512) null,
	add column default_parent_id  uuid         null,
	add column default_profile_id uuid         null,
	add column layout             json         null;

create table if not exists scope_model_parent (
	project_id            uuid    not null,
	child_scope_model_id  uuid    not null,
	parent_scope_model_id uuid    not null,
	is_default            boolean not null default false,
	parent_order          int     null,
	constraint pk_scope_model_parent primary key (project_id, child_scope_model_id, parent_scope_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists scope_model_dataset_model (
	project_id       uuid not null,
	scope_model_id   uuid not null,
	dataset_model_id uuid not null,
	constraint pk_scope_model_dataset_model primary key (project_id, scope_model_id, dataset_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists scope_model_form_model (
	project_id     uuid not null,
	scope_model_id uuid not null,
	form_model_id  uuid not null,
	constraint pk_scope_model_form_model primary key (project_id, scope_model_id, form_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists scope_model_workflow (
	project_id     uuid not null,
	scope_model_id uuid not null,
	workflow_id    uuid not null,
	constraint pk_scope_model_workflow primary key (project_id, scope_model_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists scope_model_workflow_state_selector (
	project_id        uuid not null,
	scope_model_id    uuid not null,
	workflow_id       uuid not null,
	workflow_state_id uuid not null,
	constraint pk_scope_model_workflow_state_selector primary key (project_id, scope_model_id, workflow_id, workflow_state_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table event_model
	add column event_group_id     uuid         null after scope_model_id,
	add column shortname          json         null,
	add column longname           json         null,
	add column description        json         null,
	add column inceptive          boolean      not null default false,
	add column number             int          null,
	add column mandatory          boolean      not null default false,
	add column max_occurrence     int          null,
	add column prevent_add        boolean      not null default false,
	add column deadline_value     int          null,
	add column deadline_unit      varchar(16)  null,
	add column deadline_aggr_fnct varchar(16)  null,
	add column interval_value     int          null,
	add column interval_unit      varchar(16)  null,
	add column label_pattern      varchar(512) null,
	add column icon               varchar(64)  null;

create table if not exists event_model_dataset_model (
	project_id       uuid not null,
	event_model_id   uuid not null,
	dataset_model_id uuid not null,
	constraint pk_event_model_dataset_model primary key (project_id, event_model_id, dataset_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model_form_model (
	project_id     uuid not null,
	event_model_id uuid not null,
	form_model_id  uuid not null,
	constraint pk_event_model_form_model primary key (project_id, event_model_id, form_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model_workflow (
	project_id     uuid not null,
	event_model_id uuid not null,
	workflow_id    uuid not null,
	constraint pk_event_model_workflow primary key (project_id, event_model_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model_implied_event (
	project_id             uuid not null,
	event_model_id         uuid not null,
	implied_event_model_id uuid not null,
	constraint pk_event_model_implied_event primary key (project_id, event_model_id, implied_event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model_blocked_event (
	project_id             uuid not null,
	event_model_id         uuid not null,
	blocked_event_model_id uuid not null,
	constraint pk_event_model_blocked_event primary key (project_id, event_model_id, blocked_event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_model_deadline_reference (
	project_id               uuid not null,
	event_model_id           uuid not null,
	reference_event_model_id uuid not null,
	constraint pk_event_model_deadline_reference primary key (project_id, event_model_id, reference_event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists event_group (
	event_group_id uuid         not null default uuid(),
	project_id     uuid         not null,
	scope_model_id uuid         not null,
	code           varchar(128) not null,
	shortname      json         null,
	longname       json         null,
	description    json         null,
	icon           varchar(64)  null,
	constraint pk_event_group primary key (project_id, event_group_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table dataset_model
	add column shortname               json         null after code,
	add column longname                json         null,
	add column description             json         null,
	add column multiple                boolean      not null default false,
	add column master                  boolean      not null default false,
	add column exportable              boolean      not null default false,
	add column export_order            int          null,
	add column family                  varchar(128) null,
	add column collapsed_label_pattern varchar(512) null,
	add column expanded_label_pattern  varchar(512) null;

alter table field_model
	add column type                          enum ('STRING', 'AUTO_COMPLETION', 'DATE',
		'DATE_SELECT', 'NUMBER', 'SELECT','RADIO',
		'CHECKBOX', 'CHECKBOX_GROUP', 'TEXTAREA', 'FILE')   not null after code,
	add column data_type                     enum ('STRING', 'DATE',
		'NUMBER', 'BOOLEAN', 'BLOB')                        not null,
	add column shortname                     json           null,
	add column longname                      json           null,
	add column description                   json           null,
	add column matcher_message               json           null,
	add column advanced_help                 json           null,
	add column plugin                        boolean        not null default false,
	add column searchable                    boolean        not null default false,
	add column read_only                     boolean        not null default false,
	add column exportable                    boolean        not null default false,
	add column allow_date_in_future          boolean        not null default false,
	add column export_order                  int            null,
	add column max_length                    int            null,
	add column max_integer_digits            int            null,
	add column max_decimal_digits            int            null,
	add column min_value                     decimal(18, 6) null,
	add column max_value                     decimal(18, 6) null,
	add column min_year                      int            null,
	add column dictionary                    varchar(256)   null,
	add column matcher                       varchar(128)   null,
	add column inline_help                   text           null,
	add column with_years                    boolean        not null default false,
	add column with_months                   boolean        not null default false,
	add column with_days                     boolean        not null default false,
	add column with_hours                    boolean        not null default false,
	add column with_minutes                  boolean        not null default false,
	add column with_seconds                  boolean        not null default false,
	add column years_mandatory               boolean        not null default false,
	add column months_mandatory              boolean        not null default false,
	add column days_mandatory                boolean        not null default false,
	add column hours_mandatory               boolean        not null default false,
	add column minutes_mandatory             boolean        not null default false,
	add column seconds_mandatory             boolean        not null default false,
	add column value_formula                 varchar(512)   null,
	add column possible_values_provider      varchar(128)   null,
	add column possible_values_provider_desc text           null;

create table if not exists field_model_workflow (
	project_id     uuid not null,
	field_model_id uuid not null,
	workflow_id    uuid not null,
	constraint pk_field_model_workflow primary key (project_id, field_model_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists field_possible_value (
	possible_value_id uuid         not null default uuid(),
	project_id        uuid         not null,
	field_model_id    uuid         not null,
	code              varchar(128) not null,
	shortname         json         null,
	specify           boolean      not null default false,
	export_label      varchar(256) null,
	sort_order        int          not null default 0,
	constraint pk_field_possible_value primary key (project_id, field_model_id, possible_value_id),
	constraint uq_field_possible_value_id unique (project_id, possible_value_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists field_model_validator (
	project_id     uuid not null,
	field_model_id uuid not null,
	validator_id   uuid not null,
	constraint pk_field_model_validator primary key (project_id, field_model_id, validator_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table form_model
	add column optional           boolean not null default false after code,
	add column shortname          json    null,
	add column longname           json    null,
	add column description        json    null,
	add column print_button_label json    null;

create table if not exists form_model_workflow (
	project_id    uuid not null,
	form_model_id uuid not null,
	workflow_id   uuid not null,
	constraint pk_form_model_workflow primary key (project_id, form_model_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_layout (
	form_layout_id              uuid                        not null default uuid(),
	project_id                  uuid                        not null,
	form_model_id               uuid                        not null,
	dataset_model_id            uuid                        null,
	default_sort_field_model_id uuid                        null,
	code                        varchar(128)                not null,
	type                        enum ('SINGLE', 'MULTIPLE') not null,
	description                 json                        null,
	text_before                 json                        null,
	text_after                  json                        null,
	css_code                    varchar(2048)               null,
	constraint pk_form_layout primary key (project_id, form_model_id, form_layout_id),
	constraint uq_form_layout_code unique (project_id, form_model_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_layout_column (
	project_id     uuid          not null,
	form_model_id  uuid          not null,
	form_layout_id uuid          not null,
	col_order      int           not null,
	css_code       varchar(2048) null,
	constraint pk_form_layout_column primary key (project_id, form_model_id, form_layout_id, col_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_layout_line (
	form_layout_line_id uuid not null default uuid(),
	project_id          uuid not null,
	form_model_id       uuid not null,
	form_layout_id      uuid not null,
	line_order          int  not null,
	constraint pk_form_layout_line primary key (project_id, form_model_id, form_layout_id, form_layout_line_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_layout_cell (
	form_layout_cell_id           uuid          not null default uuid(),
	project_id                    uuid          not null,
	form_model_id                 uuid          not null,
	form_layout_id                uuid          not null,
	form_layout_line_id           uuid          not null,
	dataset_model_id              uuid          null,
	field_model_id                uuid          null,
	code                          varchar(128)  not null,
	line_order                    int           not null,
	text_before                   json          null,
	text_after                    json          null,
	css_code_for_label            varchar(1024) null,
	css_code_for_input            varchar(1024) null,
	display_label                 boolean       not null default false,
	display_possible_value_labels boolean       not null default false,
	possible_values_column_number int           null,
	possible_values_column_width  int           null,
	colspan                       int           not null default 1,
	constraint pk_form_layout_cell primary key (project_id, form_model_id, form_layout_id, form_layout_line_id,
												form_layout_cell_id),
	constraint uq_form_layout_cell_id unique (project_id, form_layout_cell_id),
	constraint uq_form_layout_cell_code unique (project_id, form_model_id, form_layout_id, form_layout_line_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_cell_visibility_criteria (
	form_cell_visible_criteria_id uuid                                                       not null default uuid(),
	project_id                    uuid                                                       not null,
	form_layout_cell_id           uuid                                                       not null,
	line_order                    int                                                        not null,
	operator                      enum ('EQUALS', 'NOT_EQUALS', 'CONTAINS', 'NOT_CONTAINS', 'GREATER',
		'GREATER_EQUALS', 'LOWER', 'LOWER_EQUALS', 'NULL', 'NOT_NULL', 'BLANK', 'NOT_BLANK') null,
	action                        enum ('SHOW', 'HIDE')                                      not null,
	constraint pk_form_cell_visibility_criteria primary key (project_id, form_layout_cell_id, form_cell_visible_criteria_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_cell_visibility_criteria_target_cell (
	project_id                    uuid not null,
	form_layout_cell_id           uuid not null,
	form_cell_visible_criteria_id uuid not null,
	line_order                    int  not null,
	target_cell_id                uuid not null,
	constraint pk_form_cell_visibility_criteria_target_cell primary key (project_id, form_layout_cell_id,
																		 form_cell_visible_criteria_id, target_cell_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_cell_visibility_criteria_target_layout (
	project_id                    uuid not null,
	form_layout_cell_id           uuid not null,
	form_cell_visible_criteria_id uuid not null,
	line_order                    int  not null,
	target_layout_id              uuid not null,
	constraint pk_form_cell_visibility_criteria_target_layout primary key (project_id, form_layout_cell_id,
																		   form_cell_visible_criteria_id,
																		   target_layout_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists form_cell_visibility_criteria_value (
	project_id                    uuid         not null,
	form_layout_cell_id           uuid         not null,
	form_cell_visible_criteria_id uuid         not null,
	line_order                    int          not null,
	possible_value_id             UUID         null,
	value                         varchar(255) null,
	constraint pk_form_cell_visibility_criteria_value primary key (project_id, form_layout_cell_id,
																   form_cell_visible_criteria_id, line_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table workflow
	add column aggregate_workflow_id uuid        null after code,
	add column initial_state_id      uuid        null,
	add column creation_action_id    uuid        null,
	add column order_by              int         null,
	add column shortname             json        null,
	add column longname              json        null,
	add column description           json        null,
	add column message               json        null,
	add column mandatory             boolean     not null default false,
	add column is_unique             boolean     not null default false,
	add column icon                  varchar(64) null;

alter table workflow_state
	add column important               boolean     not null default false after code,
	add column color                   varchar(16) null,
	add column shortname               json        null,
	add column longname                json        null,
	add column description             json        null,
	add column icon                    varchar(64) null,
	add column aggregate_state_id      uuid        null,
	add column aggregate_state_matcher varchar(16) null;

create table if not exists workflow_state_possible_action (
	project_id         uuid not null,
	workflow_state_id  uuid not null,
	workflow_action_id uuid not null,
	constraint pk_workflow_state_possible_action primary key (project_id, workflow_state_id, workflow_action_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table workflow_action
	add column documentable            boolean     not null default false after code,
	add column require_signature       boolean     not null default false,
	add column shortname               json        null,
	add column longname                json        null,
	add column description             json        null,
	add column required_signature_text json        null,
	add column documentable_options    json        null,
	add column icon                    varchar(64) null;

alter table validator
	add column shortname        json    null after code,
	add column longname         json    null,
	add column description      json    null,
	add column message          json    null,
	add column required         boolean not null default false,
	add column is_script        boolean not null default false,
	add column workflow_id      uuid    null,
	add column invalid_state_id uuid    null,
	add column valid_state_id   uuid    null;

alter table payment_plan
	add column currency                varchar(16) not null after code,
	add column invoiced_scope_model_id uuid        not null,
	add column workflow_id             uuid        not null,
	add column allow_batch_merger      boolean     not null default false,
	add column extended_steps          boolean     not null default false,
	add column shortname               json        null,
	add column longname                json        null,
	add column description             json        null;

alter table payment_step
	add column repeatable     boolean not null default false after code,
	add column event_model_id uuid    not null,
	add column shortname      json    null,
	add column longname       json    null,
	add column description    json    null,
	add column sort_order     int     null,
	add constraint uq_payment_step_id unique (project_id, payment_step_id);

create table payment_step_distribution (
	payment_step_distribution_id uuid           not null default uuid(),
	project_id                   uuid           not null,
	payment_step_id              uuid           not null,
	scope_model_id               uuid           not null,
	profile_id                   uuid           not null,
	value                        decimal(18, 4) not null,
	constraint pk_payment_step_distribution primary key (project_id, payment_step_distribution_id, payment_step_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table resource_category
	add column icon        varchar(128) null after code,
	add column color       varchar(16)  null,
	add column shortname   json         null,
	add column longname    json         null,
	add column description json         null;

create table rule_definition_property (
	rule_definition_property_id uuid         not null default uuid(),
	project_id                  uuid         not null,
	code                        varchar(128) not null,
	label                       varchar(255) null,
	entity_id                   varchar(64)  not null,
	target                      varchar(64)  null,
	type                        varchar(64)  not null,
	configuration_entity        varchar(64)  null,
	constraint pk_rule_definition_property primary key (project_id, rule_definition_property_id),
	constraint uq_rule_definition_property_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table rule_definition_action (
	rule_definition_action_id uuid         not null default uuid(),
	project_id                uuid         not null,
	code                      varchar(128) not null,
	label                     varchar(255) null,
	entity_id                 varchar(64)  not null,
	constraint pk_rule_definition_action primary key (project_id, rule_definition_action_id),
	constraint uq_rule_definition_action_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table rule_definition_action_parameter (
	project_id                uuid         not null,
	rule_definition_action_id uuid         not null,
	param_code                varchar(128) not null,
	label                     varchar(255) null,
	data_entity               varchar(64)  null,
	configuration_entity      varchar(64)  null,
	sort_order                int          not null default 0,
	constraint pk_rule_definition_action_parameter primary key (project_id, rule_definition_action_id, param_code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table feature (
	feature_id  uuid         not null default uuid(),
	project_id  uuid         not null,
	code        varchar(128) not null,
	shortname   json         null,
	longname    json         null,
	description json         null,
	optional    boolean      not null default false,
	constraint pk_feature primary key (project_id, feature_id),
	constraint uq_feature_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_summary (
	workflow_summary_id    uuid         not null default uuid(),
	project_id             uuid         not null,
	code                   varchar(128) not null,
	workflow_entity        varchar(16)  not null,
	leaf_scope_model_id    uuid         null,
	filter_expected_events boolean      not null default false,
	display_legend         boolean      not null default false,
	display_column_export  boolean      not null default false,
	title                  json         null,
	constraint pk_workflow_summary primary key (project_id, workflow_summary_id),
	constraint uq_workflow_summary_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_summary_workflow (
	project_id          uuid not null,
	workflow_summary_id uuid not null,
	workflow_id         uuid not null,
	constraint pk_workflow_summary_workflow primary key (project_id, workflow_summary_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_summary_filter_event_model (
	project_id          uuid not null,
	workflow_summary_id uuid not null,
	event_model_id      uuid not null,
	constraint pk_workflow_summary_filter_event_model primary key (project_id, workflow_summary_id, event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_summary_column (
	summary_column_id   uuid        not null default uuid(),
	project_id          uuid        not null,
	workflow_summary_id uuid        not null,
	sort_order          int         not null,
	total               boolean     not null default false,
	percent             boolean     not null default false,
	non_null_color      varchar(16) null,
	non_null_bg_color   varchar(16) null,
	label               json        null,
	description         json        null,
	constraint pk_workflow_summary_column primary key (project_id, summary_column_id),
	constraint uq_workflow_summary_column_sort_order unique (project_id, workflow_summary_id, sort_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_summary_column_state (
	project_id        uuid not null,
	summary_column_id uuid not null,
	workflow_state_id uuid not null,
	constraint pk_workflow_summary_column_state primary key (project_id, summary_column_id, workflow_state_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table chart (
	chart_id             uuid            not null default uuid(),
	project_id           uuid            not null,
	code                 varchar(128)    not null,
	type                 enum ('ENROLLMENT_BY_SCOPE', 'ENROLLMENT',
		'STATISTICS', 'WORKFLOW_STATUS') not null,
	override_user_rights boolean         not null default false,
	with_statistics      boolean         not null default false,
	display_expected     boolean         not null default false,
	shortname            json            null,
	longname             json            null,
	description          json            null,
	title                json            null,
	legend_x             json            null,
	legend_y             json            null,
	workflow_id          uuid            null,
	scope_model_id       uuid            null,
	leaf_scope_model_id  uuid            null,
	dataset_model_id     uuid            null,
	field_model_id       uuid            null,
	constraint pk_chart primary key (project_id, chart_id),
	constraint uq_chart_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table chart_color (
	project_id uuid        not null,
	chart_id   uuid        not null,
	color      varchar(20) not null,
	sort_order int         not null,
	constraint pk_chart_color primary key (project_id, chart_id, sort_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table chart_range (
	chart_range_id uuid           not null default uuid(),
	project_id     uuid           not null,
	chart_id       uuid           not null,
	code           varchar(128)   not null,
	value          varchar(128)   null,
	label          json           null,
	min            decimal(18, 3) null,
	max            decimal(18, 3) null,
	is_other       boolean        not null default false,
	sort_order     int            null,
	constraint pk_chart_range primary key (project_id, chart_id, chart_range_id),
	constraint uq_chart_range_code unique (project_id, chart_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table chart_state_filter (
	project_id        uuid                                        not null,
	chart_id          uuid                                        not null,
	workflow_state_id uuid                                        not null,
	kind              enum ('INCLUDED', 'EXCLUDED', 'ENROLLMENT') not null,
	constraint pk_chart_state_filter primary key (project_id, chart_id, workflow_state_id, kind)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_widget (
	workflow_widget_id     uuid         not null default uuid(),
	project_id             uuid         not null,
	code                   varchar(128) not null,
	workflow_entity        varchar(16)  not null,
	filter_expected_events boolean      not null default false,
	shortname              json         null,
	longname               json         null,
	description            json         null,
	constraint pk_workflow_widget primary key (project_id, workflow_widget_id),
	constraint uq_workflow_widget_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_widget_state_selector (
	project_id         uuid not null,
	workflow_widget_id uuid not null,
	workflow_id        uuid not null,
	workflow_state_id  uuid not null,
	constraint pk_workflow_widget_state_selector primary key (project_id, workflow_widget_id, workflow_id, workflow_state_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table workflow_widget_column (
	workflow_widget_column_id uuid         not null default uuid(),
	project_id                uuid         not null,
	workflow_widget_id        uuid         not null,
	code                      varchar(128) not null,
	type                      varchar(64)  not null,
	width                     int          null,
	shortname                 json         null,
	longname                  json         null,
	description               json         null,
	constraint pk_workflow_widget_column primary key (project_id, workflow_widget_id, workflow_widget_column_id),
	constraint uq_workflow_widget_column_code unique (project_id, workflow_widget_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu (
	menu_id        uuid         not null default uuid(),
	project_id     uuid         not null,
	parent_menu_id uuid         null,
	sort_order     int          not null default 0,
	code           varchar(128) not null,
	order_by       int          null,
	shortname      json         null,
	longname       json         null,
	description    json         null,
	is_public      boolean      not null default false,
	is_home_page   boolean      not null default false,
	constraint pk_menu primary key (project_id, menu_id),
	constraint uq_menu_project_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_action (
	project_id uuid         not null,
	menu_id    uuid         not null,
	page       varchar(128) null,
	context    json         null,
	params     json         null,
	constraint pk_menu_action primary key (project_id, menu_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_layout_section (
	menu_section_id     uuid         not null default uuid(),
	project_id          uuid         not null,
	menu_id             uuid         not null,
	code                varchar(128) not null,
	sort_order          int          null,
	label               json         null,
	required_feature_id uuid         null,
	right_entity        varchar(64)  null,
	right_value         varchar(16)  null,
	right_target_id     uuid         null,
	constraint pk_menu_layout_section primary key (project_id, menu_id, menu_section_id),
	constraint uq_menu_layout_section_code unique (project_id, menu_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_layout_section_widget (
	menu_widget_id      uuid        not null default uuid(),
	project_id          uuid        not null,
	menu_id             uuid        not null,
	menu_section_id     uuid        not null,
	widget_order        int         not null,
	type                varchar(64) not null,
	width               varchar(16) null,
	text_before         mediumtext  null,
	required_feature_id uuid        null,
	right_entity        varchar(64) null,
	right_value         varchar(16) null,
	right_target_id     uuid        null,
	constraint pk_menu_layout_section_widget primary key (project_id, menu_id, menu_section_id, menu_widget_id),
	constraint uq_menu_widget_order unique (project_id, menu_id, menu_section_id, widget_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists menu_layout_section_widget_parameter (
	project_id          uuid        not null,
	menu_id             uuid        not null,
	menu_section_id     uuid        not null,
	menu_widget_id      uuid        not null,
	widget_order        int         not null,
	type                varchar(64) null,
	title               mediumtext  null,
	width               int         null,
	scope_model_id      uuid        null,
	workflow_widget_id  uuid        null,
	workflow_summary_id uuid        null,
	chart_id            uuid        null,
	category_id         uuid        null,
	constraint pk_menu_layout_section_widget_parameter primary key (project_id, menu_id, menu_section_id, menu_widget_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists privacy_policy (
	policy_id   uuid         not null default uuid(),
	project_id  uuid         not null,
	code        varchar(128) not null,
	shortname   json         null,
	longname    json         null,
	description json         null,
	content     json         null,
	constraint pk_privacy_policy primary key (project_id, policy_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists privacy_policy_profile (
	project_id uuid not null,
	policy_id  uuid not null,
	profile_id uuid not null,
	constraint pk_privacy_policy_profile primary key (project_id, policy_id, profile_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists report (
	report_id        uuid         not null default uuid(),
	project_id       uuid         not null,
	code             varchar(128) not null,
	workflow_id      uuid         not null,
	dataset_model_id uuid         null,
	shortname        json         null,
	longname         json         null,
	description      json         null,
	constraint pk_report primary key (project_id, report_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists report_field (
	project_id     uuid not null,
	report_id      uuid not null,
	field_model_id uuid not null,
	constraint pk_report_field primary key (project_id, report_id, field_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph (
	timeline_graph_id          uuid         not null default uuid(),
	project_id                 uuid         not null,
	code                       varchar(128) not null,
	scope_model_id             uuid         null,
	study_start_event_model_id uuid         null,
	study_period_is_default    boolean      not null default false,
	height                     int          null,
	legend_width               int          null,
	scroller_height            int          null,
	show_scroller              boolean      not null default false,
	shortname                  json         null,
	longname                   json         null,
	description                json         null,
	footnote                   json         null,
	constraint pk_timeline_graph primary key (project_id, timeline_graph_id),
	constraint uq_timeline_graph_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph_section (
	graph_section_id     uuid                                   not null default uuid(),
	project_id           uuid                                   not null,
	timeline_graph_id    uuid                                   not null,
	code                 varchar(128)                           not null,
	type                 enum ('ACTION','PERIOD','DATE','LINE') not null,
	dataset_model_id     uuid                                   null,
	date_field_id        uuid                                   null,
	end_date_field_id    uuid                                   null,
	label_field_id       uuid                                   null,
	value_field_id       uuid                                   null,
	hide_expected_event  boolean                                not null default false,
	hide_done_event      boolean                                not null default false,
	use_scope_paths      boolean                                not null default false,
	color                varchar(16)                            null,
	stroke_color         varchar(16)                            null,
	opacity              decimal(6, 3)                          null,
	dashed               boolean                                not null default false,
	mark                 enum ('CIRCLE','SQUARE','DIAMOND')     null,
	position_start       int                                    null,
	position_stop        int                                    null,
	scale_min            decimal(18, 6)                         null,
	scale_max            decimal(18, 6)                         null,
	scale_decimal        int                                    null,
	scale_mark_interval  decimal(18, 6)                         null,
	scale_label_interval decimal(18, 6)                         null,
	scale_position       enum ('LEFT','RIGHT')                  null,
	hidden_legend        boolean                                not null default false,
	hidden               boolean                                not null default false,
	label                json                                   null,
	tooltip              json                                   null,
	constraint pk_timeline_graph_section primary key (project_id, timeline_graph_id, graph_section_id),
	constraint uq_timeline_graph_section_code unique (project_id, timeline_graph_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph_section_meta_field (
	project_id        uuid not null,
	timeline_graph_id uuid not null,
	graph_section_id  uuid not null,
	field_model_id    uuid not null,
	sort_order        int  null,
	constraint pk_timeline_graph_section_meta_field primary key (project_id, timeline_graph_id, graph_section_id, field_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph_section_event (
	project_id        uuid not null,
	timeline_graph_id uuid not null,
	graph_section_id  uuid not null,
	event_model_id    uuid not null,
	sort_order        int  null,
	constraint pk_timeline_graph_section_event primary key (project_id, timeline_graph_id, graph_section_id, event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph_section_reference (
	graph_reference_id   uuid        not null default uuid(),
	project_id           uuid        not null,
	timeline_graph_id    uuid        not null,
	graph_section_id     uuid        not null,
	color                varchar(16) null,
	dashed               boolean     not null default false,
	reference_section_id uuid        null,
	label                json        null,
	tooltip              json        null,
	constraint pk_timeline_graph_section_reference primary key (project_id, timeline_graph_id, graph_section_id, graph_reference_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists timeline_graph_section_reference_entry (
	project_id         uuid           not null,
	timeline_graph_id  uuid           not null,
	graph_section_id   uuid           not null,
	graph_reference_id uuid           not null,
	timepoint          varchar(32)    not null,
	value              decimal(18, 6) null,
	label              varchar(128)   null,
	sort_order         int            null,
	constraint pk_timeline_graph_section_reference_entry primary key (project_id, timeline_graph_id, graph_section_id,
																	  graph_reference_id, timepoint)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

alter table profile
	add column order_by                int  null after code,
	add column workflow_of_interest_id uuid null,
	add column shortname               json null,
	add column longname                json null,
	add column description             json null;

create table if not exists profile_profile_rights (
	project_id        uuid    not null,
	profile_id        uuid    not null,
	target_profile_id uuid    not null,
	can_read          boolean not null default false,
	can_write         boolean not null default false,
	constraint pk_profile_profile_rights primary key (project_id, profile_id, target_profile_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_dataset_model_rights (
	project_id       uuid    not null,
	profile_id       uuid    not null,
	dataset_model_id uuid    not null,
	can_read         boolean not null default false,
	can_write        boolean not null default false,
	constraint pk_profile_dataset_model_rights primary key (project_id, profile_id, dataset_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_scope_model_rights (
	project_id     uuid    not null,
	profile_id     uuid    not null,
	scope_model_id uuid    not null,
	can_read       boolean not null default false,
	can_write      boolean not null default false,
	constraint pk_profile_scope_model_rights primary key (project_id, profile_id, scope_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_payment_model_rights (
	project_id      uuid    not null,
	profile_id      uuid    not null,
	payment_plan_id uuid    not null,
	can_read        boolean not null default false,
	can_write       boolean not null default false,
	constraint pk_profile_payment_model_rights primary key (project_id, profile_id, payment_plan_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_event_model_rights (
	project_id     uuid    not null,
	profile_id     uuid    not null,
	event_model_id uuid    not null,
	can_read       boolean not null default false,
	can_write      boolean not null default false,
	constraint pk_profile_event_model_rights primary key (project_id, profile_id, event_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_form_model_rights (
	project_id    uuid    not null,
	profile_id    uuid    not null,
	form_model_id uuid    not null,
	can_read      boolean not null default false,
	can_write     boolean not null default false,
	constraint pk_profile_form_model_rights primary key (project_id, profile_id, form_model_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_workflow_rights (
	project_id  uuid    not null,
	profile_id  uuid    not null,
	workflow_id uuid    not null,
	has_right   boolean not null default false,
	constraint pk_profile_workflow_rights primary key (project_id, profile_id, workflow_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_workflow_action_rights (
	project_id         uuid    not null,
	profile_id         uuid    not null,
	workflow_action_id uuid    not null,
	granted_by_system  boolean not null default false,
	constraint pk_profile_workflow_action_rights primary key (project_id, profile_id, workflow_action_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_feature_grants (
	project_id uuid not null,
	profile_id uuid not null,
	feature_id uuid not null,
	constraint pk_profile_feature_grant primary key (project_id, profile_id, feature_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_menu_grants (
	project_id uuid not null,
	profile_id uuid not null,
	menu_id    uuid not null,
	constraint pk_profile_menu_grants primary key (project_id, profile_id, menu_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_category_grants (
	project_id  uuid not null,
	profile_id  uuid not null,
	category_id uuid not null,
	constraint pk_profile_category_grants primary key (project_id, profile_id, category_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_timeline_graph_grants (
	project_id        uuid not null,
	profile_id        uuid not null,
	timeline_graph_id uuid not null,
	constraint pk_profile_timeline_graph_grants primary key (project_id, profile_id, timeline_graph_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists profile_report_grants (
	project_id uuid not null,
	profile_id uuid not null,
	report_id  uuid not null,
	constraint pk_profile_report_grants primary key (project_id, profile_id, report_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists cron (
	cron_id        uuid         not null default uuid(),
	project_id     uuid         not null,
	code           varchar(128) not null,
	interval_value int          not null,
	interval_unit  varchar(16)  not null,
	description    json         null,
	constraint pk_cron primary key (project_id, cron_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule (
	rule_id     uuid                                                                   not null default uuid(),
	project_id  uuid                                                                   not null,
	entity_type enum ('CRON', 'DATASET_MODEL', 'SCOPE_MODEL', 'WORKFLOW',
		'WORKFLOW_ACTION', 'FIELD_MODEL', 'FORM_MODEL', 'EVENT_MODEL', 'EVENT_ACTION') not null,
	entity_id   uuid                                                                   not null,
	rule_type   varchar(32)                                                            null,
	description varchar(1024)                                                          null,
	message     json                                                                   null,
	tag         json                                                                   null,
	constraint pk_rule primary key (project_id, rule_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_action (
	rule_action_id   uuid         not null default uuid(),
	project_id       uuid         not null,
	rule_id          uuid         not null,
	code             varchar(128) not null,
	action_id_code   varchar(128) null,
	static_action_id varchar(128) not null,
	optional         boolean      not null default false,
	label            json         null,
	condition_id     uuid         null,
	rulable_entity   varchar(32)  null,
	action_order     int          null,
	constraint pk_rule_action primary key (project_id, rule_action_id),
	constraint uq_rule_action_code unique (project_id, code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_action_parameter (
	rule_action_parameter_id uuid          not null default uuid(),
	project_id               uuid          not null,
	rule_action_id           uuid          not null,
	code                     varchar(128)  null,
	value                    varchar(1024) null,
	ruling_entity            varchar(32)   null,
	condition_id             varchar(128)  null,
	constraint pk_rule_action_parameter primary key (project_id, rule_action_id, rule_action_parameter_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists selection_node (
	selection_id        uuid         not null default uuid(),
	project_id          uuid         not null,
	parent_selection_id uuid         null,
	node_entity         varchar(32)  not null,
	node_code           varchar(128) not null,
	node_id             uuid         null,
	sort_order          int          null,
	constraint pk_selection_node primary key (project_id, selection_id),
	constraint uq_selection_node_sibling unique (project_id, parent_selection_id, node_entity, node_code)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_constraint (
	constraint_id   uuid                                                                  not null default uuid(),
	project_id      uuid                                                                  not null,
	owner_type      enum ('RULE', 'FIELD_MODEL',
		'VALIDATOR', 'FORM_LAYOUT', 'FORM_LAYOUT_CELL')                                   not null,
	owner_id        uuid                                                                  not null, -- e.g. rule.rule_id or validator.validator_id
	constraint_type enum ('DEFAULT', 'VISIBILITY', 'VALUE_FORMULA', 'VALIDATION', 'RULE') not null default 'DEFAULT',
	created_at      timestamp(3)                                                          not null default current_timestamp(3),
	evaluations     json                                                                  null,
	constraint pk_rule_constraint primary key (project_id, constraint_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_condition_list (
	condition_list_id uuid                                                            not null default uuid(),
	project_id        uuid                                                            not null,
	constraint_id     uuid                                                            not null,
	domain            enum ('SCOPE', 'DATASET', 'WORKFLOW', 'FIELD', 'FORM', 'EVENT') not null,
	mode              enum ('AND', 'OR')                                              not null,
	constraint pk_rule_condition_list primary key (project_id, condition_list_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_condition (
	condition_id        uuid               not null default uuid(),
	project_id          uuid               not null,
	condition_list_id   uuid               not null,
	parent_condition_id uuid               null,
	code                varchar(128)       null,
	mode                enum ('AND', 'OR') not null,
	inverse             boolean            not null default false,
	dependency          boolean            not null default false,
	break_type          varchar(16)        null,
	constraint pk_rule_condition primary key (project_id, condition_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_criterion (
	criterion_id uuid        not null default uuid(),
	project_id   uuid        not null,
	condition_id uuid        not null,
	property     varchar(64) not null,
	operator     varchar(32) null,
	constraint pk_rule_criterion primary key (project_id, criterion_id)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;

create table if not exists rule_criterion_value (
	project_id   uuid         not null,
	criterion_id uuid         not null,
	value_order  int          not null,
	value_text   varchar(512) not null,
	constraint pk_rule_criterion_value primary key (project_id, criterion_id, value_order)
) engine = InnoDB
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci;
