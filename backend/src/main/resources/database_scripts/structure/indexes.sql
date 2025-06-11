/* event */
alter table event add index idx_event_date (date);
alter table event add index idx_event_event_model_id (event_model_id);
alter table event add index idx_event_blocking (blocking);

/* workflow status */
alter table workflow_status add index idx_workflow_status_workflow_id (workflow_id);
alter table workflow_status add index idx_workflow_status_state_id (state_id);

/* role */
alter table role add index idx_role_profile_id (profile_id);

/* user */
alter table user add index idx_user_name (name);
alter table user add index idx_user_email (email);

/* mail */
alter table mail add index idx_mail_status (status);
alter table mail add index idx_mail_origin (origin);
alter table mail add index idx_mail_sender (sender);
alter table mail add index idx_mail_subject (subject);

/* deleted indices */
alter table dataset add index idx_dataset_deleted (deleted);
alter table form add index idx_form_deleted (deleted);
alter table payment add index idx_payment_deleted (deleted);
alter table payment_batch add index idx_payment_batch_deleted (deleted);
alter table payment_target add index idx_payment_target_deleted (deleted);
alter table resource add index idx_resource_deleted (deleted);
alter table robot add index idx_robot_deleted (deleted);
alter table scope add index idx_scope_deleted (deleted);
alter table user add index idx_user_deleted (deleted);
alter table event add index idx_event_deleted (deleted);
alter table workflow_status add index idx_workflow_status_deleted (deleted);

/* aggregate workflow states indices */
alter table workflow_status add index aggregate_scope (workflow_id, deleted, scope_fk);
alter table workflow_status add index aggregate_event (workflow_id, deleted, event_fk, form_fk, field_fk);

/* chart */
alter table chart add index idx_chart_chart_id (chart_id);