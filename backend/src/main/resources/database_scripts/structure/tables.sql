/* to be able to remove the tables, disable foreign key checks */
set FOREIGN_KEY_CHECKS=0;

/* ==========================================================
   TENANCY ANCHOR
   ========================================================== */
drop table if exists project;
create table if not exists project (
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    url varchar(512) null,
    email varchar(255) null,
    color varchar(9) null,
    introduction_text mediumtext null,
    smtp_tls boolean not null default false,
    password_strong boolean not null default false,
    password_length int null,
    password_validity_duration int null,
    password_unique boolean not null default false,
    epro_enabled boolean not null default false,
    epro_profile_id uuid null,
    client_name varchar(255) null,
    client_email varchar(255) null,
    protocol_no varchar(64) null,
    version_number varchar(32) null,
    version_date date null,
    config_version int null,
    config_date bigint null,
    config_user varchar(128) null,
    constraint pk_project primary key (project_id),
    constraint uq_project_code unique (code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists project_language;
create table if not exists project_language (
    project_id uuid not null,
    language varchar(8) not null,
    is_default boolean not null default false,
    constraint pk_project_language primary key (project_id, language)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists project_rule_tag;
create table if not exists project_rule_tag (
    project_id uuid not null,
    tag varchar(64) not null,
    constraint pk_project_rule_tag primary key (project_id, tag)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* internal patch */
drop table if exists internal_patch;
create table internal_patch (
    project_id uuid not null,
	script double(20, 2) not null,
	date datetime(3) not null default now(3),
	context varchar(500) not null,
	name varchar(80) not null,
	constraint pk_internal_patch primary key (script)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

insert into internal_patch (project_id, script, date, context, name) select  p.project_id, 179, now(3), 'Remove country from user', 'db_update_179.sql' from project p;

/***********************************
*                                  *
*            BUSINESS              *
*                                  *
***********************************/

/* scope */
drop table if exists scope;
create table scope (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_model_id uuid not null,
	code varchar(200) default null,
	shortname varchar(256) default null,
	longname varchar(512) default null,
	start_date datetime(3) default null,
	stop_date datetime(3) default null,
	`virtual` boolean not null default false,
	color varchar(64) default null,
	expected_number int(11) default null,
	max_number int(11) default null,
	locked boolean not null default false,
	data longtext not null,
	constraint pk_scope primary key (pk),
	constraint u_scope_id unique (id),
	constraint u_scope_code_project unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_audit;
create table scope_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	id varchar(200) not null,
	deleted boolean not null default false,
	scope_model_id uuid default null,
	code varchar(200) default null,
	shortname varchar(256) default null,
	longname varchar(512) default null,
	start_date datetime(3) default null,
	stop_date datetime(3) default null,
	`virtual` boolean not null default false,
	color varchar(64) default null,
	expected_number int(11) default null,
	max_number int(11) default null,
	locked boolean not null default false,
	data longtext not null,
	constraint pk_scope_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model;
create table if not exists scope_model (
    scope_model_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    plural_shortname json null,
    virtual boolean not null default false,
    max_number int null,
    scope_format varchar(512) null,
	default_parent_id uuid null,
    default_profile_id uuid null,
    layout json null,
    constraint pk_scope_model primary key (project_id, scope_model_id),
    constraint uq_scope_model_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model_parent;
create table if not exists scope_model_parent (
    project_id uuid not null,
    child_scope_model_id uuid not null,
    parent_scope_model_id uuid not null,
    is_default boolean not null default false,
    parent_order int null,
    constraint pk_scope_model_parent primary key (project_id, child_scope_model_id, parent_scope_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model_dataset_model;
create table if not exists scope_model_dataset_model (
    project_id uuid not null,
    scope_model_id uuid not null,
    dataset_model_id uuid not null,
    constraint pk_scope_model_dataset_model primary key (project_id, scope_model_id, dataset_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model_form_model;
create table if not exists scope_model_form_model (
    project_id uuid not null,
    scope_model_id uuid not null,
    form_model_id uuid not null,
    constraint pk_scope_model_form_model primary key (project_id, scope_model_id, form_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model_workflow;
create table if not exists scope_model_workflow (
    project_id uuid not null,
    scope_model_id uuid not null,
    workflow_id uuid not null,
    constraint pk_scope_model_workflow primary key (project_id, scope_model_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_model_workflow_state_selector;
create table if not exists scope_model_workflow_state_selector (
    project_id uuid not null,
    scope_model_id uuid not null,
    workflow_id uuid not null,
    workflow_state_id uuid not null,
    constraint pk_scope_model_workflow_state_selector primary key (project_id, scope_model_id, workflow_id, workflow_state_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* scope relation */
drop table if exists scope_relation;
create table scope_relation (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	scope_fk bigint(20) not null,
	parent_fk bigint(20) not null,
	start_date datetime(3) not null default now(3),
	end_date datetime(3) default null,
	`default` boolean not null default false,
	constraint pk_scope_relation primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* event */
drop table if exists event;
create table event (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) not null,
	scope_model_id uuid not null,
	event_group_number int(11) not null,
	event_model_id uuid not null,
	expected_date datetime(3) default null,
	date datetime(3) default null,
	end_date datetime(3) default null,
	not_done boolean not null default true,
	blocking boolean not null default false,
	locked boolean not null default false,
	constraint pk_event primary key (pk),
	constraint u_event_id unique (id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_audit;
create table event_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	id varchar(200) not null,
	deleted boolean not null default false,
	scope_fk bigint(20) not null,
	scope_model_id uuid not null,
	event_group_number int(11) not null,
	event_model_id uuid not null,
	expected_date datetime(3) default null,
	date datetime(3) default null,
	end_date datetime(3) default null,
	not_done boolean not null default true,
	blocking boolean not null default false,
	locked boolean not null default false,
	constraint pk_event_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model;
create table if not exists event_model (
    event_model_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    event_group_id uuid null,
    scope_model_id uuid null,
    shortname json null,
    longname json null,
    description json null,
    inceptive boolean not null default false,
    number int null,
    mandatory boolean not null default false,
    max_occurrence int null,
    prevent_add boolean not null default false,
    deadline_value int null,
    deadline_unit varchar(16) null,
    deadline_aggr_fnct varchar(16) null,
    interval_value int null,
    interval_unit varchar(16) null,
    label_pattern varchar(512) null,
    icon varchar(64) null,
    constraint pk_event_model primary key (project_id, event_model_id),
    constraint uq_event_model_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_dataset_model;
create table if not exists event_model_dataset_model (
    project_id uuid not null,
    event_model_id uuid not null,
    dataset_model_id uuid not null,
    constraint pk_event_model_dataset_model primary key (project_id, event_model_id, dataset_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_form_model;
create table if not exists event_model_form_model (
    project_id uuid not null,
    event_model_id uuid not null,
    form_model_id uuid not null,
    constraint pk_event_model_form_model primary key (project_id, event_model_id, form_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_workflow;
create table if not exists event_model_workflow (
    project_id uuid not null,
    event_model_id uuid not null,
    workflow_id uuid not null,
    constraint pk_event_model_workflow primary key (project_id, event_model_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_implied_event;
create table if not exists event_model_implied_event (
    project_id uuid not null,
    event_model_id uuid not null,
    implied_event_model_id uuid not null,
    constraint pk_event_model_implied_event primary key (project_id, event_model_id, implied_event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_blocked_event;
create table if not exists event_model_blocked_event (
    project_id uuid not null,
    event_model_id uuid not null,
    blocked_event_model_id uuid not null,
    constraint pk_event_model_blocked_event primary key (project_id, event_model_id, blocked_event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_model_deadline_reference;
create table if not exists event_model_deadline_reference (
    project_id uuid not null,
    event_model_id uuid not null,
    reference_event_model_id uuid not null,
    constraint pk_event_model_deadline_reference primary key (project_id, event_model_id, reference_event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists event_group;
create table if not exists event_group (
    event_group_id uuid not null default uuid(),
    project_id uuid not null,
    scope_model_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_event_group primary key (project_id, event_group_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* dataset */
drop table if exists dataset;
create table dataset (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	dataset_model_id uuid not null,
	constraint pk_dataset primary key (pk),
	constraint u_dataset_id unique (id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists dataset_audit;
create table dataset_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	id varchar(200) not null,
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	dataset_model_id uuid not null,
	constraint pk_dataset_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists dataset_model;
create table if not exists dataset_model (
    dataset_model_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    multiple boolean not null default false,
    master boolean not null default false,
    exportable boolean not null default false,
    export_order int null,
    family varchar(128) null,
    collapsed_label_pattern varchar(512) null,
    expanded_label_pattern varchar(512) null,
    constraint pk_dataset_model primary key (project_id, dataset_model_id),
    constraint uq_dataset_model_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* field */
drop table if exists field;
create table field (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	dataset_fk bigint(20) not null,
	dataset_model_id uuid not null,
	field_model_id uuid not null,
	value text,
	constraint pk_field primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_audit;
create table field_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	dataset_fk bigint(20) not null,
	dataset_model_id uuid not null,
	field_model_id uuid not null,
	value text,
	constraint pk_field_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_model;
create table if not exists field_model (
    field_model_id uuid not null default uuid(),
    project_id uuid not null,
    dataset_model_id uuid not null,
    code varchar(128) not null,
    type varchar(32) not null,
    data_type varchar(32) not null,
    shortname json null,
    longname json null,
    description json null,
    matcher_message json null,
    advanced_help json null,
    plugin boolean not null default false,
    searchable boolean not null default false,
    read_only boolean not null default false,
    exportable boolean not null default false,
    allow_date_in_future boolean not null default false,
    export_order int null,
    max_length int null,
    max_integer_digits int null,
    max_decimal_digits int null,
    min_value decimal(18, 6) null,
    max_value decimal(18, 6) null,
    min_year int null,
    dictionary varchar(256) null,
    matcher varchar(128) null,
    inline_help text null,
    with_years boolean not null default false,
    with_months boolean not null default false,
    with_days boolean not null default false,
    with_hours boolean not null default false,
    with_minutes boolean not null default false,
    with_seconds boolean not null default false,
    years_mandatory boolean not null default false,
    months_mandatory boolean not null default false,
    days_mandatory boolean not null default false,
    hours_mandatory boolean not null default false,
    minutes_mandatory boolean not null default false,
    seconds_mandatory boolean not null default false,
    value_formula varchar(512) null,
    possible_values_provider varchar(128) null,
    possible_values_provider_desc text null,
    constraint pk_field_model primary key (project_id, field_model_id),
    constraint uq_field_model_code unique (project_id, dataset_model_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_model_workflow;
create table if not exists field_model_workflow (
    project_id uuid not null,
    field_model_id uuid not null,
    workflow_id uuid not null,
    constraint pk_field_model_workflow primary key (project_id, field_model_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_possible_value;
create table if not exists field_possible_value (
    possible_value_id uuid not null default uuid(),
    project_id uuid not null,
    field_model_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    specify boolean not null default false,
    export_label varchar(256) null,
    sort_order int not null default 0,
    constraint pk_field_possible_value primary key (project_id, field_model_id, possible_value_id),
	constraint uq_field_possible_value_id unique (project_id, possible_value_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_model_validator;
create table if not exists field_model_validator (
    project_id uuid not null,
    field_model_id uuid not null,
    validator_id uuid not null,
    constraint pk_field_model_validator primary key (project_id, field_model_id, validator_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* form */
drop table if exists form;
create table form (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_model_id uuid not null,
	constraint pk_form primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_audit;
create table form_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_model_id uuid not null,
	constraint pk_form_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_model;
create table if not exists form_model (
    form_model_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    optional boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    print_button_label json null,
    constraint pk_form_model primary key (project_id, form_model_id),
    constraint uq_form_model_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_model_workflow;
create table if not exists form_model_workflow (
    project_id uuid not null,
    form_model_id uuid not null,
    workflow_id uuid not null,
    constraint pk_form_model_workflow primary key (project_id, form_model_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_layout;
create table if not exists form_layout (
    form_layout_id uuid not null default uuid(),
    project_id uuid not null,
    form_model_id uuid not null,
    dataset_model_id uuid null,
    default_sort_field_model_id uuid null,
    code varchar(128) not null,
    type varchar(16) not null,
    description json null,
    text_before json null,
    text_after json null,
    css_code varchar(2048) null,
    constraint pk_form_layout primary key (project_id, form_model_id, form_layout_id),
    constraint uq_form_layout_code unique (project_id, form_model_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_layout_column;
create table if not exists form_layout_column (
    project_id uuid not null,
    form_model_id uuid not null,
    form_layout_id uuid not null,
    col_order int not null,
    css_code varchar(2048) null,
    constraint pk_form_layout_column primary key (project_id, form_model_id, form_layout_id, col_order)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_layout_line;
create table if not exists form_layout_line (
    form_layout_line_id uuid not null default uuid(),
    project_id uuid not null,
    form_model_id uuid not null,
    form_layout_id uuid not null,
    line_order int not null,
    constraint pk_form_layout_line primary key (project_id, form_model_id, form_layout_id, form_layout_line_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_layout_cell;
create table if not exists form_layout_cell (
    form_layout_cell_id uuid not null default uuid(),
    project_id uuid not null,
    form_model_id uuid not null,
    form_layout_id uuid not null,
    form_layout_line_id uuid not null,
    dataset_model_id uuid null,
    field_model_id uuid null,
    code varchar(128) not null,
    line_order int not null,
    text_before json null,
    text_after json null,
    css_code_for_label varchar(1024) null,
    css_code_for_input varchar(1024) null,
    display_label boolean not null default false,
    display_possible_value_labels boolean not null default false,
    possible_values_column_number int null,
    possible_values_column_width int null,
    colspan int not null default 1,
    constraint pk_form_layout_cell primary key (project_id, form_model_id, form_layout_id, form_layout_line_id, form_layout_cell_id),
    constraint uq_form_layout_cell_code unique (project_id, form_model_id, form_layout_id, form_layout_line_id, code),
	constraint uq_form_layout_cell_id unique (project_id, form_layout_cell_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_cell_visibility_criteria;
create table if not exists form_cell_visibility_criteria (
    form_cell_visible_criteria_id uuid not null default uuid(),
    project_id uuid not null,
    form_layout_cell_id uuid not null,
    line_order int not null,
    operator varchar(32) null,
    action varchar(16) not null,
    constraint pk_form_cell_visibility_criteria primary key (project_id, form_layout_cell_id, form_cell_visible_criteria_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_cell_visibility_criteria_target_cell;
create table if not exists form_cell_visibility_criteria_target_cell (
    project_id uuid not null,
    form_layout_cell_id uuid not null,
    form_cell_visible_criteria_id uuid not null,
    line_order int not null,
    target_cell_id uuid not null,
    constraint pk_form_cell_visibility_criteria_target_cell primary key (project_id, form_layout_cell_id, form_cell_visible_criteria_id, target_cell_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_cell_visibility_criteria_target_layout;
create table if not exists form_cell_visibility_criteria_target_layout (
    project_id uuid not null,
    form_layout_cell_id uuid not null,
    form_cell_visible_criteria_id uuid not null,
    line_order int not null,
    target_layout_id uuid not null,
    constraint pk_form_cell_visibility_criteria_target_layout primary key (project_id, form_layout_cell_id, form_cell_visible_criteria_id, target_layout_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_cell_visibility_criteria_value;
create table if not exists form_cell_visibility_criteria_value (
    project_id uuid not null,
    form_layout_cell_id uuid not null,
    form_cell_visible_criteria_id uuid not null,
    line_order int not null,
    possible_value_id UUID not null,
    constraint pk_form_cell_visibility_criteria_value primary key (project_id, form_layout_cell_id, form_cell_visible_criteria_id, possible_value_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* workflow */
drop table if exists workflow_status;
create table workflow_status (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_fk bigint(20) default null,
	field_fk bigint(20) default null,
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	profile_id uuid default null,
	workflow_state_id uuid not null,
	workflow_id uuid not null,
	workflow_action_id uuid default null,
	validator_id uuid default null,
	trigger_message varchar(1000) default null,
	constraint pk_workflow_status primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_status_audit;
create table workflow_status_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_fk bigint(20) default null,
	field_fk bigint(20) default null,
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	profile_id uuid default null,
	workflow_state_id uuid not null,
	workflow_id uuid not null,
	workflow_action_id uuid default null,
	validator_id uuid default null,
	trigger_message varchar(1000) default null,
	constraint pk_workflow_status_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow;
create table if not exists workflow (
    workflow_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    aggregate_workflow_id uuid null,
    initial_state_id uuid null,
    order_by int null,
    shortname json null,
    longname json null,
    description json null,
    message json null,
    mandatory boolean not null default false,
    is_unique boolean not null default false,
    icon varchar(64) null,
    constraint pk_workflow primary key (project_id, workflow_id),
    constraint uq_workflow_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_state;
create table if not exists workflow_state (
    workflow_state_id uuid not null default uuid(),
    project_id uuid not null,
    workflow_id uuid not null,
    code varchar(128) not null,
    state_order int null,
    important boolean not null default false,
    color varchar(16) null,
    shortname json null,
    longname json null,
    description json null,
    icon varchar(64) null,
    aggregate_state_id uuid null,
    aggregate_state_matcher varchar(16) null,
    constraint pk_workflow_state primary key (project_id, workflow_state_id),
    constraint uq_workflow_state_code unique (project_id, workflow_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_state_possible_action;
create table if not exists workflow_state_possible_action (
    project_id uuid not null,
    workflow_state_id uuid not null,
    workflow_action_id uuid not null,
    constraint pk_workflow_state_possible_action primary key (project_id, workflow_state_id, workflow_action_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_action;
create table if not exists workflow_action (
    workflow_action_id uuid not null default uuid(),
    project_id uuid not null,
    workflow_id uuid not null,
    code varchar(128) not null,
    action_order int null,
    documentable boolean not null default false,
    require_signature boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    required_signature_text json null,
    documentable_options json null,
    constraint pk_workflow_action primary key (project_id, workflow_action_id),
    constraint uq_workflow_action_code unique (project_id, workflow_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* validator */
drop table if exists validator;
create table if not exists validator (
    validator_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    message json null,
    required boolean not null default false,
    is_script boolean not null default false,
    workflow_id uuid null,
    invalid_state_id uuid null,
    valid_state_id uuid null,
    constraint pk_validator primary key (project_id, validator_id),
    constraint uq_validator_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* payment */
drop table if exists payment;
create table payment (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	payment_batch_fk bigint(20) not null,
	workflow_status_fk bigint(20) not null,
	payment_plan_id uuid not null,
	payment_step_id uuid default null,
	status varchar(50) default null,
	value int(11) default null,
	constraint pk_payment primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_batch;
create table payment_batch (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_id varchar(200) default null,
	payment_plan_id uuid default null,
	status varchar(50) default null,
	payment_date datetime(3) default null,
	closed_date datetime(3) default null,
	printed_date datetime(3) default null,
	comment varchar(500) default null,
	constraint pk_payment_batch primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_target;
create table payment_target (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	payment_fk bigint(20) not null,
	payable_id varchar(100) default null,
	value float(11, 2) not null,
	constraint pk_payment_target primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_plan;
create table payment_plan (
    payment_plan_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    currency varchar(16) not null,
    invoiced_scope_model_id uuid not null,
    workflow_id uuid not null,
    allow_batch_merger boolean not null default false,
    extended_steps boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_payment_plan primary key (project_id, payment_plan_id),
    constraint uq_payment_plan_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_step;
create table payment_step (
    payment_step_id uuid not null default uuid(),
    project_id uuid not null,
    payment_plan_id uuid not null,
    code varchar(128) not null,
    repeatable boolean not null default false,
    event_model_id uuid not null,
    shortname json null,
    longname json null,
    description json null,
    sort_order int null,
    constraint pk_payment_step primary key (project_id, payment_plan_id, payment_step_id),
    constraint uq_payment_step_code unique (project_id, code),
	constraint uq_payment_step_id unique (project_id, payment_step_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_step_distribution;
create table payment_step_distribution (
    payment_step_distribution_id uuid not null default uuid(),
    project_id uuid not null,
    payment_step_id uuid not null,
    scope_model_id uuid not null,
    profile_id uuid not null,
    value decimal(18, 4) not null,
    constraint pk_payment_step_distribution primary key (project_id, payment_step_distribution_id, payment_step_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* role */
drop table if exists role;
create table role (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	scope_fk bigint(20) not null,
	profile_id uuid not null,
	status varchar(50) not null,
	constraint pk_role primary key (pk)
) engine = InnoDB auto_increment=20 default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists role_audit;
create table role_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	scope_fk bigint(20) not null,
	profile_id uuid not null,
	status varchar(50) not null,
	constraint pk_role_audit primary key (pk)
) engine = InnoDB auto_increment=20 default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* user */
drop table if exists user;
create table user (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	name varchar(400) not null,
	email varchar(200) not null,
	externally_managed boolean not null default false,
	activated boolean not null default false,
	activation_code char(36) default null,
	recovery_code char(36) default null,
	pending_email varchar(200) default null,
	email_modification_date datetime(3) default null,
	email_verification_code char(36) default null,
	password varchar(60) default null,
	password_changed_date datetime(3) default null,
	previous_passwords text default null,
	password_attempts int(6) not null default 0,
	password_reset_code char(36) default null,
	password_reset_date datetime(3) default null,
	login_date datetime(3) default null,
	previous_login_date datetime(3) default null,
	logout_date datetime(3) default null,
	login_blocking_date datetime(3) default null,
	should_change_password boolean not null default false,
	phone varchar(50) default null,
	language_id varchar(3) default null,
	user_agent varchar(500) default null,
	constraint pk_user primary key (pk),
	constraint u_email unique (email)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists user_audit;
create table user_audit (
	pk bigint(20) not null auto_increment,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	deleted boolean not null default false,
	name varchar(400) not null,
	email varchar(200) not null,
	externally_managed boolean not null default false,
	activated boolean not null default false,
	activation_code char(36) default null,
	recovery_code char(36) default null,
	pending_email varchar(200) default null,
	email_modification_date datetime(3) default null,
	email_verification_code char(36) default null,
	password varchar(60) default null,
	password_changed_date datetime(3) default null,
	previous_passwords text default null,
	password_attempts int(6) not null default 0,
	password_reset_code char(36) default null,
	password_reset_date datetime(3) default null,
	login_date datetime(3) default null,
	previous_login_date datetime(3) default null,
	logout_date datetime(3) default null,
	login_blocking_date datetime(3) default null,
	should_change_password boolean not null default false,
	phone varchar(50) default null,
	language_id varchar(3) default null,
	user_agent varchar(500) default null,
	constraint pk_user_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists user_session;
create table user_session (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	token varchar(32) not null,
	user_fk bigint(20) not null,
	last_access_time datetime(3) not null default now(3),
	constraint pk_user_session primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* mail */
drop table if exists mail;
create table mail (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	attempts int(11) not null default '0',
	status varchar(32) not null,
	error varchar(500) default null,
	sent_time datetime(3) default null,
	origin varchar(32) default null,
	intent varchar(255) default null,
	sender varchar(255) not null,
	recipients text not null,
	reply_to varchar(64) default null,
	subject varchar(255) not null,
	text_body text not null,
	html_body text default null,
	constraint pk_mail primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists mail_attachment;
create table mail_attachment (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	mail_fk bigint(20) not null,
	filename varchar(255) not null,
	content blob not null,
	constraint pk_mail_attachment primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* resource */
drop table if exists resource;
create table resource (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	user_fk bigint(20) not null,
	scope_fk bigint(20) not null,
	uuid varchar(255) not null default uuid(),
	title varchar(100) not null,
	description text default null,
	category_id uuid not null,
	public_resource boolean default 0,
	filename varchar(100) default null,
	constraint pk_resource primary key (pk),
	constraint u_resource_uuid unique (uuid)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists resource_category;
create table resource_category (
    category_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    icon varchar(128) null,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_resource_category primary key (project_id, category_id),
    constraint uq_resource_category_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* robot */
drop table if exists robot;
create table robot (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean default false,
	name varchar(100) not null,
	`key` varchar(255) not null,
	activated boolean default false,
	constraint pk_robot primary key (pk),
	constraint u_robot_name_project unique (project_id, name),
	constraint u_robot_key_project unique (project_id, `key`)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists robot_audit;
create table robot_audit (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	deleted boolean default false,
	name varchar(100) not null,
	`key` varchar(255) not null,
	activated boolean default false,
	constraint pk_robot_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* file */
drop table if exists file;
create table file (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	dataset_fk bigint(20) default null,
	field_fk bigint(20) default null,
	trail_fk bigint(20) default null,
	user_fk bigint(20) not null,
	uuid varchar(255) not null,
	name varchar(255) not null,
	checksum varbinary(40) not null,
	submitted boolean not null default false,
	constraint pk_file primary key (pk),
	constraint u_uuid unique (uuid)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists audit_action;
create table audit_action (
	pk bigint(20) not null auto_increment,
    project_id uuid not null,
	date datetime(3) not null default now(3),
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	context text not null,
	constraint pk_audit_action primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/***********************************
*                                  *
*          CONFIG TABLES           *
*                                  *
***********************************/

/* rule definition */
drop table if exists rule_definition_property;
create table rule_definition_property (
    rule_definition_property_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    label varchar(255) null,
    entity_id varchar(64) not null,
    target varchar(64) null,
    type varchar(64) not null,
    configuration_entity varchar(64) null,
    constraint pk_rule_definition_property primary key (project_id, rule_definition_property_id),
    constraint uq_rule_definition_property_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_definition_action;
create table rule_definition_action (
    rule_definition_action_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    label varchar(255) null,
    entity_id varchar(64) not null,
    constraint pk_rule_definition_action primary key (project_id, rule_definition_action_id),
    constraint uq_rule_definition_action_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_definition_action_parameter;
create table rule_definition_action_parameter (
    project_id uuid not null,
    rule_definition_action_id uuid not null,
    param_code varchar(128) not null,
    label varchar(255) null,
    data_entity varchar(64) null,
    configuration_entity varchar(64) null,
    sort_order int not null default 0,
    constraint pk_rule_definition_action_parameter primary key (project_id, rule_definition_action_id, param_code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* feature */
drop table if exists feature;
create table feature (
    feature_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    optional boolean not null default false,
    constraint pk_feature primary key (project_id, feature_id),
    constraint uq_feature_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* workflow summary */
drop table if exists workflow_summary;
create table workflow_summary (
    workflow_summary_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    workflow_entity varchar(16) not null,
    leaf_scope_model_id uuid null,
    filter_expected_events boolean not null default false,
    display_legend boolean not null default false,
    display_column_export boolean not null default false,
    title json null,
    constraint pk_workflow_summary primary key (project_id, workflow_summary_id),
    constraint uq_workflow_summary_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_summary_workflow;
create table workflow_summary_workflow (
    project_id uuid not null,
    workflow_summary_id uuid not null,
    workflow_id uuid not null,
    constraint pk_workflow_summary_workflow primary key (project_id, workflow_summary_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_summary_filter_event_model;
create table workflow_summary_filter_event_model (
    project_id uuid not null,
    workflow_summary_id uuid not null,
    event_model_id uuid not null,
    constraint pk_workflow_summary_filter_event_model primary key (project_id, workflow_summary_id, event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_summary_column;
create table workflow_summary_column (
    summary_column_id uuid not null default uuid(),
    project_id uuid not null,
    workflow_summary_id uuid not null,
    sort_order int not null,
    total boolean not null default false,
    percent boolean not null default false,
    non_null_color varchar(16) null,
    non_null_bg_color varchar(16) null,
    label json null,
    description json null,
    constraint pk_workflow_summary_column primary key (project_id, summary_column_id),
    constraint uq_workflow_summary_column_sort_order unique (project_id, workflow_summary_id, sort_order)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_summary_column_state;
create table workflow_summary_column_state (
    project_id uuid not null,
    summary_column_id uuid not null,
    workflow_state_id uuid not null,
    constraint pk_workflow_summary_column_state primary key (project_id, summary_column_id, workflow_state_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* chart */
drop table if exists chart;
create table chart (
    chart_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    type varchar(64)  not null,
    override_user_rights boolean not null default false,
    with_statistics boolean not null default false,
    display_expected boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    title json null,
    legend_x json null,
    legend_y json null,
    workflow_id uuid null,
    scope_model_id uuid null,
    leaf_scope_model_id uuid null,
    dataset_model_id uuid null,
    field_model_id uuid null,
    constraint pk_chart primary key (project_id, chart_id),
    constraint uq_chart_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists chart_color;
create table chart_color (
    project_id uuid not null,
    chart_id uuid not null,
    color varchar(20) not null,
    sort_order int not null,
    constraint pk_chart_color primary key (project_id, chart_id, sort_order)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists chart_range;
create table chart_range (
    chart_range_id uuid not null default uuid(),
    project_id uuid not null,
    chart_id uuid not null,
    code varchar(128) not null,
    value varchar(128) null,
    label json null,
    min decimal(18, 3) null,
    max decimal(18, 3) null,
    is_other boolean not null default false,
    sort_order int null,
    constraint pk_chart_range primary key (project_id, chart_id, chart_range_id),
    constraint uq_chart_range_code unique (project_id, chart_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists chart_state_filter;
create table chart_state_filter (
    project_id uuid not null,
    chart_id uuid not null,
    workflow_state_id uuid not null,
    kind enum ('INCLUDED', 'EXCLUDED', 'ENROLLMENT') not null,
    constraint pk_chart_state_filter primary key (project_id, chart_id, workflow_state_id, kind)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* workflow widget */
drop table if exists workflow_widget;
create table workflow_widget (
    workflow_widget_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    workflow_entity varchar(16) not null,
    filter_expected_events boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_workflow_widget primary key (project_id, workflow_widget_id),
    constraint pk_workflow_widget_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_widget_state_selector;
create table workflow_widget_state_selector (
    project_id uuid not null,
    workflow_widget_id uuid not null,
    workflow_id uuid not null,
    workflow_state_id uuid not null,
    constraint pk_workflow_widget_state_selector primary key (project_id, workflow_widget_id, workflow_id, workflow_state_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_widget_column;
create table workflow_widget_column (
    workflow_widget_column_id uuid not null default uuid(),
    project_id uuid not null,
    workflow_widget_id uuid not null,
    code varchar(128) not null,
    type varchar(64) not null,
    width int null,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_workflow_widget_column primary key (project_id, workflow_widget_id, workflow_widget_column_id),
    constraint uq_workflow_widget_column_code unique (project_id, workflow_widget_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* menu */
drop table if exists menu;
create table if not exists menu (
    menu_id uuid not null default uuid(),
    project_id uuid not null,
    parent_menu_id uuid null,
    sort_order int not null default 0,
    code varchar(128) not null,
    order_by int null,
    shortname json null,
    longname json null,
    description json null ,
    is_public boolean not null default false,
    is_home_page boolean not null default false,
    constraint pk_menu primary key (project_id, menu_id),
    constraint uq_menu_project_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists menu_action;
create table if not exists menu_action (
    project_id uuid not null,
    menu_id uuid not null,
    page varchar(128) null,
    context json null,
    params json null,
    constraint pk_menu_action primary key (project_id, menu_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists menu_layout_section;
create table if not exists menu_layout_section (
    menu_section_id uuid not null default uuid(),
    project_id uuid not null,
    menu_id uuid not null,
    code varchar(128) not null,
    sort_order int null,
    label json null,
    required_feature_id uuid null,
    right_entity varchar(64) null,
    right_value varchar(16) null,
    right_target_id uuid null,
    constraint pk_menu_layout_section primary key (project_id, menu_id, menu_section_id),
    constraint uq_menu_layout_section_code unique (project_id, menu_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists menu_layout_section_widget;
create table if not exists menu_layout_section_widget (
    menu_widget_id uuid not null default uuid(),
    project_id uuid not null,
    menu_id uuid not null,
    menu_section_id uuid not null,
    widget_order int not null,
    type varchar(64) not null,
    width varchar(16) null,
    text_before mediumtext null,
    required_feature_id uuid null,
    right_entity varchar(64) null,
    right_value varchar(16) null,
    right_target_id uuid null,
    constraint pk_menu_layout_section_widget primary key (project_id, menu_id, menu_section_id, menu_widget_id),
    constraint uq_menu_widget_order unique (project_id, menu_id, menu_section_id, widget_order)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists menu_layout_section_widget_parameter;
create table if not exists menu_layout_section_widget_parameter (
    project_id uuid not null,
    menu_id uuid not null,
    menu_section_id uuid not null,
    menu_widget_id uuid not null,
    widget_order int not null,
    type varchar(64) null,
    title mediumtext null,
    width int null,
    scope_model_id uuid null,
    workflow_widget_id uuid null,
    workflow_summary_id uuid null,
    chart_id uuid null,
    category_id uuid null,
    constraint pk_menu_layout_section_widget_parameter primary key (project_id, menu_id, menu_section_id, menu_widget_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* privacy policy */
drop table if exists privacy_policy;
create table if not exists privacy_policy (
    policy_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    shortname json null,
    longname json null,
    description json null,
    content json null,
    constraint pk_privacy_policy primary key (project_id, policy_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists privacy_policy_profile;
create table if not exists privacy_policy_profile (
    project_id uuid not null,
    policy_id  uuid not null,
    profile_id uuid not null,
    constraint pk_privacy_policy_profile primary key (project_id, policy_id, profile_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* report */
drop table if exists report;
create table if not exists report (
    report_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    dataset_model_id uuid not null,
    workflow_id uuid not null,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_report primary key (project_id, report_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists report_field;
create table if not exists report_field (
    project_id uuid not null,
    report_id uuid not null,
    field_model_id uuid not null,
    constraint pk_report_field primary key (project_id, report_id, field_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* timeline graph */
drop table if exists timeline_graph;
create table if not exists timeline_graph (
    timeline_graph_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    scope_model_id uuid null,
    study_start_event_model_id uuid null,
    study_period_is_default boolean not null default false,
    height int null,
    legend_width int null,
    scroller_height int null,
    show_scroller boolean not null default false,
    shortname json null,
    longname json null,
    description json null,
    footnote json null,
    constraint pk_timeline_graph primary key (project_id, timeline_graph_id),
    constraint uq_timeline_graph_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists timeline_graph_section;
create table if not exists timeline_graph_section (
    graph_section_id uuid not null default uuid(),
    project_id uuid not null,
    timeline_graph_id uuid not null,
    code varchar(128) not null,
    type enum ('ACTION','PERIOD','DATE','LINE') not null,
    dataset_model_id uuid null,
    date_field_id uuid null,
    end_date_field_id uuid null,
    label_field_id uuid null,
    value_field_id uuid null,
    hide_expected_event boolean not null default false,
    hide_done_event boolean not null default false,
    use_scope_paths boolean not null default false,
    color varchar(16) null,
    stroke_color varchar(16) null,
    opacity decimal(6, 3) null,
    dashed boolean not null default false,
    mark enum ('CIRCLE','SQUARE','DIAMOND') null,
    position_start int null,
    position_stop int null,
    scale_min decimal(18, 6) null,
    scale_max decimal(18, 6) null,
    scale_decimal int null,
    scale_mark_interval decimal(18, 6) null,
    scale_label_interval decimal(18, 6) null,
    scale_position enum ('LEFT','RIGHT') null,
    hidden_legend boolean not null default false,
    hidden boolean not null default false,
    label json null,
    tooltip json null,
    constraint pk_timeline_graph_section primary key (project_id, timeline_graph_id, graph_section_id),
    constraint uq_timeline_graph_section_code unique (project_id, timeline_graph_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists timeline_graph_section_meta_field;
create table if not exists timeline_graph_section_meta_field (
    project_id uuid not null,
    timeline_graph_id uuid not null,
    graph_section_id uuid not null,
    field_model_id uuid not null,
    sort_order int null,
    constraint pk_timeline_graph_section_meta_field primary key (project_id, timeline_graph_id, graph_section_id, field_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists timeline_graph_section_event;
create table if not exists timeline_graph_section_event (
    project_id uuid not null,
    timeline_graph_id uuid not null,
    graph_section_id uuid not null,
    event_model_id uuid not null,
    sort_order int null,
    constraint pk_timeline_graph_section_event primary key (project_id, timeline_graph_id, graph_section_id, event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists timeline_graph_section_reference;
create table if not exists timeline_graph_section_reference (
    graph_reference_id uuid not null default uuid(),
    project_id uuid not null,
    timeline_graph_id uuid not null,
    graph_section_id uuid not null,
    color varchar(16) null,
    dashed boolean not null default false,
    reference_section_id uuid null,
    label json null,
    tooltip json null,
    constraint pk_timeline_graph_section_reference primary key (project_id, timeline_graph_id, graph_section_id, graph_reference_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists timeline_graph_section_reference_entry;
create table if not exists timeline_graph_section_reference_entry (
    project_id uuid not null,
    timeline_graph_id uuid not null,
    graph_section_id uuid not null,
    graph_reference_id uuid not null,
    timepoint varchar(32) not null,
    value decimal(18, 6) null,
    label varchar(128) null,
    sort_order int null,
    constraint pk_timeline_graph_section_reference_entry primary key (project_id, timeline_graph_id, graph_section_id, graph_reference_id, timepoint)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* profile */
drop table if exists profile;
create table if not exists profile (
    profile_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    order_by int null,
    workflow_of_interest_id uuid null,
    shortname json null,
    longname json null,
    description json null,
    constraint pk_profile primary key (project_id, profile_id),
    constraint uq_profile_code unique (project_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_profile_rights;
create table if not exists profile_profile_rights (
    project_id uuid not null,
    profile_id uuid not null,
    target_profile_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_profile_rights primary key (project_id, profile_id, target_profile_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_dataset_model_rights;
create table if not exists profile_dataset_model_rights (
    project_id uuid not null,
    profile_id uuid not null,
    dataset_model_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_dataset_model_rights primary key (project_id, profile_id, dataset_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_scope_model_rights;
create table if not exists profile_scope_model_rights (
    project_id uuid not null,
    profile_id uuid not null,
    scope_model_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_scope_model_rights primary key (project_id, profile_id, scope_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_payment_model_rights;
create table if not exists profile_payment_model_rights (
    project_id uuid not null,
    profile_id uuid not null,
    payment_plan_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_payment_model_rights primary key (project_id, profile_id, payment_plan_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_event_model_rights;
create table if not exists profile_event_model_rights (
    project_id uuid not null,
    profile_id uuid not null,
    event_model_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_event_model_rights primary key (project_id, profile_id, event_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_form_model_rights;
create table if not exists profile_form_model_rights (
    project_id uuid not null,
    profile_id uuid not null,
    form_model_id uuid not null,
    can_read boolean not null default false,
    can_write boolean not null default false,
    constraint pk_profile_form_model_rights primary key (project_id, profile_id, form_model_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_workflow_rights;
create table if not exists profile_workflow_rights (
    project_id uuid not null,
    profile_id uuid not null,
    workflow_id uuid not null,
    has_right boolean not null default false,
    constraint pk_profile_workflow_rights primary key (project_id, profile_id, workflow_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_workflow_action_rights;
create table if not exists profile_workflow_action_rights (
    project_id uuid not null,
    profile_id uuid not null,
    workflow_action_id uuid not null,
    granted_by_system boolean not null default false,
    constraint pk_profile_workflow_action_rights primary key (project_id, profile_id, workflow_action_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_feature_grants;
create table if not exists profile_feature_grants (
    project_id uuid not null,
    profile_id uuid not null,
    feature_id uuid not null,
    constraint pk_profile_feature_grant primary key (project_id, profile_id, feature_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_menu_grants;
create table if not exists profile_menu_grants (
    project_id uuid not null,
    profile_id uuid not null,
    menu_id uuid not null,
    constraint pk_profile_menu_grants primary key (project_id, profile_id, menu_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_category_grants;
create table if not exists profile_category_grants (
    project_id uuid not null,
    profile_id uuid not null,
    category_id uuid not null,
    constraint pk_profile_category_grants primary key (project_id, profile_id, category_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_timeline_graph_grants;
create table if not exists profile_timeline_graph_grants (
    project_id uuid not null,
    profile_id uuid not null,
    timeline_graph_id uuid not null,
    constraint pk_profile_timeline_graph_grants primary key (project_id, profile_id, timeline_graph_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists profile_report_grants;
create table if not exists profile_report_grants (
    project_id uuid not null,
    profile_id uuid not null,
    report_id uuid not null,
    constraint pk_profile_report_grants primary key (project_id, profile_id, report_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* cron */
drop table if exists cron;
create table if not exists cron (
    cron_id uuid not null default uuid(),
    project_id uuid not null,
    code varchar(128) not null,
    interval_value int not null,
    interval_unit varchar(16) not null,
    description json null,
    constraint pk_cron primary key (project_id, cron_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* rule */
drop table if exists rule;
create table if not exists rule (
    rule_id uuid not null default uuid(),
    project_id uuid not null,
    entity_type enum ('CRON', 'DATASET_MODEL', 'SCOPE_MODEL', 'WORKFLOW',
        'WORKFLOW_ACTION', 'FIELD_MODEL', 'FORM_MODEL', 'EVENT_MODEL', 'EVENT_ACTION') not null,
    entity_id uuid not null,
    rule_type varchar(32) null,
    description varchar(1024) null,
    message json null,
    tag json null,
    constraint pk_rule primary key (project_id, rule_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_action;
create table if not exists rule_action (
    rule_action_id uuid not null default uuid(),
    project_id uuid not null,
    rule_id uuid not null,
    code varchar(128) not null,
    action_id_code varchar(128) null,
    static_action_id varchar(128) null,
    optional boolean not null default false,
    label json null,
    condition_id uuid null,
    rulable_entity varchar(32) null,
    action_order int null,
    constraint pk_rule_action primary key (project_id, rule_action_id),
    constraint uq_rule_action_code unique (project_id, rule_id, code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_action_parameter;
create table if not exists rule_action_parameter (
    rule_action_parameter_id uuid not null default uuid(),
    project_id uuid not null,
    rule_action_id uuid not null,
    code varchar(128) null,
    value varchar(1024) null,
    ruling_entity varchar(32) null,
    condition_id varchar(128) null,
    constraint pk_rule_action_parameter primary key (project_id, rule_action_id, rule_action_parameter_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* selection */
drop table if exists selection_node;
create table if not exists selection_node (
    selection_id uuid not null default uuid(),
    project_id uuid not null,
    parent_selection_id uuid null,
    node_entity varchar(32) not null,
    node_code varchar(128) not null,
    node_id uuid null,
    sort_order int null,
    constraint pk_selection_node primary key (project_id, selection_id),
    constraint uq_selection_node_sibling unique (project_id, parent_selection_id, node_entity, node_code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* rule constraint */
drop table if exists rule_constraint;
create table if not exists rule_constraint (
    constraint_id uuid not null default uuid(),
    project_id uuid not null,
    owner_type enum ('RULE', 'FIELD_MODEL',
        'VALIDATOR', 'FORM_LAYOUT', 'FORM_LAYOUT_CELL') not null,
    owner_id uuid not null, -- e.g. rule.rule_id or validator.validator_id
    created_at timestamp(3) not null default current_timestamp(3),
    evaluations json null,
    constraint pk_rule_constraint primary key (project_id, constraint_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_condition_list;
create table if not exists rule_condition_list (
    condition_list_id uuid not null default uuid(),
    project_id uuid not null,
    constraint_id uuid not null,
    domain enum ('SCOPE', 'DATASET', 'WORKFLOW', 'FIELD', 'FORM', 'EVENT') not null,
    mode enum ('AND', 'OR') not null,
    constraint pk_rule_condition_list primary key (project_id, condition_list_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_condition;
create table if not exists rule_condition (
    condition_id uuid not null default uuid(),
    project_id uuid not null,
    condition_list_id uuid not null,
    parent_condition_id uuid null,
    code varchar(128) null,
    mode enum ('AND', 'OR') not null,
    inverse boolean not null default false,
    dependency boolean not null default false,
    break_type varchar(16) null,
    constraint pk_rule_condition primary key (project_id, condition_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_criterion;
create table if not exists rule_criterion (
    criterion_id uuid not null default uuid(),
    project_id uuid not null,
    condition_id uuid not null,
    property varchar(64) not null,
    operator varchar(32) null,
    constraint pk_rule_criterion primary key (project_id, criterion_id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists rule_criterion_value;
create table if not exists rule_criterion_value (
    project_id uuid not null,
    criterion_id uuid not null,
    value_order int not null,
    value_text varchar(512) not null,
    constraint pk_rule_criterion_value primary key (project_id, criterion_id, value_order)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* never forget to re-enable foreign key checks */
set FOREIGN_KEY_CHECKS=1;
