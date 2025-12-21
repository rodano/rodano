/* ==========================================================
   ADD project_id TO ALL EXISTING TABLES
   ========================================================== */

insert ignore into project (project_id, code)
values (uuid(), 'TEST');

set @project_id = (select project_id from project where code = 'TEST' limit 1);

alter table audit_action add column project_id uuid null first;
alter table dataset add column project_id uuid null first;
alter table dataset_audit add column project_id uuid null first;
alter table event add column project_id uuid null first;
alter table event_audit add column project_id uuid null first;
alter table field add column project_id uuid null first;
alter table field_audit add column project_id uuid null first;
alter table file add column project_id uuid null first;
alter table form add column project_id uuid null first;
alter table form_audit add column project_id uuid null first;
alter table internal_patch add column project_id uuid null first;
alter table mail add column project_id uuid null first;
alter table mail_attachment add column project_id uuid null first;
alter table payment add column project_id uuid null first;
alter table payment_batch add column project_id uuid null first;
alter table payment_target add column project_id uuid null first;
alter table resource add column project_id uuid null first;
alter table robot add column project_id uuid null first;
alter table robot_audit add column project_id uuid null first;
alter table role add column project_id uuid null first;
alter table role_audit add column project_id uuid null first;
alter table scope add column project_id uuid null first;
alter table scope_audit add column project_id uuid null first;
alter table scope_relation add column project_id uuid null first;
alter table workflow_status add column project_id uuid null first;
alter table workflow_status_audit add column project_id uuid null first;

-- Populate project_id
update audit_action set project_id = @project_id where project_id is null;
update dataset set project_id = @project_id where project_id is null;
update dataset_audit set project_id = @project_id where project_id is null;
update event set project_id = @project_id where project_id is null;
update event_audit set project_id = @project_id where project_id is null;
update field set project_id = @project_id where project_id is null;
update field_audit set project_id = @project_id where project_id is null;
update file set project_id = @project_id where project_id is null;
update form set project_id = @project_id where project_id is null;
update form_audit set project_id = @project_id where project_id is null;
update internal_patch set project_id = @project_id where project_id is null;
update mail set project_id = @project_id where project_id is null;
update mail_attachment set project_id = @project_id where project_id is null;
update payment set project_id = @project_id where project_id is null;
update payment_batch set project_id = @project_id where project_id is null;
update payment_target set project_id = @project_id where project_id is null;
update resource set project_id = @project_id where project_id is null;
update robot set project_id = @project_id where project_id is null;
update robot_audit set project_id = @project_id where project_id is null;
update role set project_id = @project_id where project_id is null;
update role_audit set project_id = @project_id where project_id is null;
update scope set project_id = @project_id where project_id is null;
update scope_audit set project_id = @project_id where project_id is null;
update scope_relation set project_id = @project_id where project_id is null;
update workflow_status set project_id = @project_id where project_id is null;
update workflow_status_audit set project_id = @project_id where project_id is null;

-- Make project_id not null
alter table audit_action modify column project_id uuid not null;
alter table dataset modify column project_id uuid not null;
alter table dataset_audit modify column project_id uuid not null;
alter table event modify column project_id uuid not null;
alter table event_audit modify column project_id uuid not null;
alter table field modify column project_id uuid not null;
alter table field_audit modify column project_id uuid not null;
alter table file modify column project_id uuid not null;
alter table form modify column project_id uuid not null;
alter table form_audit modify column project_id uuid not null;
alter table internal_patch modify column project_id uuid not null;
alter table mail modify column project_id uuid not null;
alter table mail_attachment modify column project_id uuid not null;
alter table payment modify column project_id uuid not null;
alter table payment_batch modify column project_id uuid not null;
alter table payment_target modify column project_id uuid not null;
alter table resource modify column project_id uuid not null;
alter table robot modify column project_id uuid not null;
alter table robot_audit modify column project_id uuid not null;
alter table role modify column project_id uuid not null;
alter table role_audit modify column project_id uuid not null;
alter table scope modify column project_id uuid not null;
alter table scope_audit modify column project_id uuid not null;
alter table scope_relation modify column project_id uuid not null;
alter table workflow_status modify column project_id uuid not null;
alter table workflow_status_audit modify column project_id uuid not null;
