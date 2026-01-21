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

/* removed indices */
alter table dataset add index idx_dataset_removed (removed);
alter table form add index idx_form_removed (removed);
alter table payment add index idx_payment_removed (removed);
alter table payment_batch add index idx_payment_batch_removed (removed);
alter table payment_target add index idx_payment_target_removed (removed);
alter table resource add index idx_resource_removed (removed);
alter table robot add index idx_robot_removed (removed);
alter table scope add index idx_scope_removed (removed);
alter table user add index idx_user_removed (removed);
alter table event add index idx_event_removed (removed);
alter table workflow_status add index idx_workflow_status_removed (removed);

/* aggregate workflow states indices */
alter table workflow_status add index aggregate_scope (workflow_id, removed, scope_fk);
alter table workflow_status add index aggregate_event (workflow_id, removed, event_fk, form_fk, field_fk);

/* user session */
alter table user_session add unique index idx_user_session_token (token);

/* indices for search optimization*/
alter table scope_relation add index idx_scope_relation_parent_start (parent_fk, start_date);
alter table scope_relation add index idx_scope_relation_parent_end (parent_fk, end_date);

alter table workflow_status add index idx_workflow_status_scope_workflow (scope_fk, workflow_id);
alter table workflow_status add index idx_workflow_status_filter (workflow_id, deleted, state_id, scope_fk);

alter table dataset add index idx_dataset_scope_model (scope_fk, dataset_model_id);

alter table field add index idx_field_dataset_model (dataset_fk, field_model_id);
alter table field add index idx_field_value (value);
