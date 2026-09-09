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

alter table field add index idx_field_value (value);
alter table field add index idx_field_field_model_id (field_model_id);

alter table dataset add index idx_dataset_dataset_model_id (dataset_model_id);


