/* to be able to remove the tables, disable foreign key checks */
set FOREIGN_KEY_CHECKS=0;

/* internal patch */
drop table if exists internal_patch;
create table internal_patch (
	script double(20, 2) not null,
	date datetime(3) not null default now(3),
	context varchar(500) not null,
	name varchar(80) not null,
	constraint pk_internal_patch primary key (script)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

insert into internal_patch (script, date, context, name) values (179, now(3), 'Remove country from user', 'db_update_179.sql');

/***********************************
*                                  *
*            BUSINESS              *
*                                  *
***********************************/

/* scope */
drop table if exists scope;
create table scope (
	pk bigint(20) not null auto_increment,
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_model_id varchar(64) not null,
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
	constraint u_scope_code unique (code)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists scope_audit;
create table scope_audit (
	pk bigint(20) not null auto_increment,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	id varchar(200) not null,
	deleted boolean not null default false,
	scope_model_id varchar(64) default null,
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

/* scope relation */
drop table if exists scope_relation;
create table scope_relation (
	pk bigint(20) not null auto_increment,
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
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) not null,
	scope_model_id varchar(100) not null,
	event_group_number int(11) not null,
	event_model_id varchar(100) not null,
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
	scope_model_id varchar(100) not null,
	event_group_number int(11) not null,
	event_model_id varchar(100) not null,
	expected_date datetime(3) default null,
	date datetime(3) default null,
	end_date datetime(3) default null,
	not_done boolean not null default true,
	blocking boolean not null default false,
	locked boolean not null default false,
	constraint pk_event_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* dataset */
drop table if exists dataset;
create table dataset (
	pk bigint(20) not null auto_increment,
	id varchar(200) not null,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	dataset_model_id varchar(100) not null,
	constraint pk_dataset primary key (pk),
	constraint u_dataset_id unique (id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists dataset_audit;
create table dataset_audit (
	pk bigint(20) not null auto_increment,
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
	dataset_model_id varchar(100) not null,
	constraint pk_dataset_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* field */
drop table if exists field;
create table field (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	dataset_fk bigint(20) not null,
	dataset_model_id varchar(100) not null,
	field_model_id varchar(100) not null,
	value text,
	constraint pk_field primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists field_audit;
create table field_audit (
	pk bigint(20) not null auto_increment,
	audit_action_fk bigint(20) not null,
	audit_datetime datetime(3) not null,
	audit_actor varchar(200) not null,
	audit_user_fk bigint(20) default null,
	audit_robot_fk bigint(20) default null,
	audit_context text not null,
	audit_object_fk bigint(20) not null,
	dataset_fk bigint(20) not null,
	dataset_model_id varchar(100) not null,
	field_model_id varchar(100) not null,
	value text,
	constraint pk_field_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* form */
drop table if exists form;
create table form (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_model_id varchar(100) not null,
	constraint pk_form primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists form_audit;
create table form_audit (
	pk bigint(20) not null auto_increment,
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
	form_model_id varchar(100) not null,
	constraint pk_form_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* workflow status */
drop table if exists workflow_status;
create table workflow_status (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_fk bigint(20) default null,
	event_fk bigint(20) default null,
	form_fk bigint(20) default null,
	field_fk bigint(20) default null,
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	profile_id varchar(100) default null,
	state_id varchar(100) not null,
	workflow_id varchar(100) not null,
	action_id varchar(100) default null,
	validator_id varchar(100) default null,
	trigger_message varchar(1000) default null,
	constraint pk_workflow_status primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists workflow_status_audit;
create table workflow_status_audit (
	pk bigint(20) not null auto_increment,
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
	profile_id varchar(100) default null,
	state_id varchar(100) not null,
	workflow_id varchar(100) not null,
	action_id varchar(100) default null,
	validator_id varchar(100) default null,
	trigger_message varchar(1000) default null,
	constraint pk_workflow_status_audit primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* payment */
drop table if exists payment;
create table payment (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	payment_batch_fk bigint(20) not null,
	workflow_status_fk bigint(20) not null,
	plan_id varchar(50) not null,
	step_id varchar(100) default null,
	status varchar(50) default null,
	value int(11) default null,
	constraint pk_payment primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists payment_batch;
create table payment_batch (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	scope_id varchar(200) default null,
	plan_id varchar(50) default null,
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
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	payment_fk bigint(20) not null,
	payable_id varchar(100) default null,
	value float(11, 2) not null,
	constraint pk_payment_target primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* role */
drop table if exists role;
create table role (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	scope_fk bigint(20) not null,
	profile_id varchar(50) not null,
	status varchar(50) not null,
	constraint pk_role primary key (pk)
) engine = InnoDB auto_increment=20 default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists role_audit;
create table role_audit (
	pk bigint(20) not null auto_increment,
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
	profile_id varchar(50) not null,
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
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean not null default false,
	user_fk bigint(20) not null,
	scope_fk bigint(20) not null,
	uuid varchar(255) not null default uuid(),
	title varchar(100) not null,
	description text default null,
	category_id varchar(50) not null,
	public_resource boolean default 0,
	filename varchar(100) default null,
	constraint pk_resource primary key (pk),
	constraint u_resource_uuid unique (uuid)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* robot */
drop table if exists robot;
create table robot (
	pk bigint(20) not null auto_increment,
	creation_time datetime(3) not null default now(3),
	last_update_time datetime(3) not null default now(3),
	deleted boolean default false,
	name varchar(100) not null,
	`key` varchar(255) not null,
	activated boolean default false,
	constraint pk_robot primary key (pk),
	constraint u_robot_name unique (name),
	constraint u_robot_key unique (`key`)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

drop table if exists robot_audit;
create table robot_audit (
	pk bigint(20) not null auto_increment,
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
	date datetime(3) not null default now(3),
	user_fk bigint(20) default null,
	robot_fk bigint(20) default null,
	context text not null,
	constraint pk_audit_action primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* chart */
drop table if exists chart;
create table chart (
	pk bigint(20) not null auto_increment,
	chart_id varchar(100) not null,
	title varchar(255),
	x_label varchar(255),
	y_label varchar(255),
	chart_type varchar(100) not null,

	-- chartConfig fields
	graph_type varchar(100) not null,
	unit_format varchar(100),
	ignore_na boolean,
	show_x_axis_label boolean,
	show_y_axis_label boolean,
	show_data_labels boolean,
	data_label_pos varchar(100),
	data_label_format varchar(100),
	show_legend boolean,
	show_gridlines boolean,
	background_color varchar(20),
	header_color varchar(20),

	-- requestParams fields
	workflow_id varchar(100),
	leaf_scope_model_id varchar(100),
	dataset_model_id varchar(100),
	field_model_id varchar(100),
	event_model_id varchar(100),
	scope_model_id varchar(100),
	show_other_category boolean,
	ignore_user_rights boolean,

	constraint pk_chart primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* chart_config_color */
drop table if exists chart_color;
create table chart_color (
	pk bigint(20) not null auto_increment,
	chart_fk bigint(20) not null,
	color_order int not null,
	color varchar(20) not null,
	constraint pk_chart_color primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* chart_state */
drop table if exists chart_state;
create table chart_state (
	pk bigint(20) not null auto_increment,
	chart_fk bigint(20) not null,
	state_id varchar(100) not null,
	constraint pk_chart_state primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* chart_category */
drop table if exists chart_category;
create table chart_category (
	pk bigint(20) not null auto_increment,
	chart_fk bigint(20) not null,
	label varchar(100),
	min varchar(100),
	max varchar(100),
	`show` boolean,
	constraint pk_chart_category primary key (pk)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

/* never forget to re-enable foreign key checks */
set FOREIGN_KEY_CHECKS=1;
