insert into internal_patch (script, date, context, name) values (182, now(3), 'Rename deleted column to removed', 'db_update_182.sql');

ALTER TABLE scope CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE scope_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE event CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE event_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE dataset CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE dataset_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE form CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE form_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE workflow_status CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE workflow_status_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE payment CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE payment_batch CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE payment_target CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE user CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE user_audit CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE resource CHANGE COLUMN deleted removed BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE robot CHANGE COLUMN deleted removed BOOLEAN DEFAULT false;
ALTER TABLE robot_audit CHANGE COLUMN deleted removed BOOLEAN DEFAULT false;

ALTER TABLE scope RENAME INDEX idx_scope_deleted TO idx_scope_removed;
ALTER TABLE event RENAME INDEX idx_event_deleted TO idx_event_removed;
ALTER TABLE dataset RENAME INDEX idx_dataset_deleted TO idx_dataset_removed;
ALTER TABLE form RENAME INDEX idx_form_deleted TO idx_form_removed;
ALTER TABLE workflow_status RENAME INDEX idx_workflow_status_deleted TO idx_workflow_status_removed;
ALTER TABLE payment RENAME INDEX idx_payment_deleted TO idx_payment_removed;
ALTER TABLE payment_batch RENAME INDEX idx_payment_batch_deleted TO idx_payment_batch_removed;
ALTER TABLE payment_target RENAME INDEX idx_payment_target_deleted TO idx_payment_target_removed;
ALTER TABLE user RENAME INDEX idx_user_deleted TO idx_user_removed;
ALTER TABLE resource RENAME INDEX idx_resource_deleted TO idx_resource_removed;
ALTER TABLE robot RENAME INDEX idx_robot_deleted TO idx_robot_removed;

/* aggregate_scope and aggregate_event keep their names; their deleted column is renamed to removed by the CHANGE COLUMN statements above */
