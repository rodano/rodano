/* ==========================================================
   MIGRATE ALL DATA TABLES FROM STRING CODES TO UUIDS
   ========================================================== */

set @project_id = (select project_id
				   from project
				   where code = 'TEST'
				   limit 1);

set SQL_SAFE_UPDATES = 0;
set FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- SCOPE
-- ============================================================
alter table scope
	add column scope_model_uuid uuid null after scope_model_id;

update scope s inner join scope_model sm on s.scope_model_id = sm.code and sm.project_id = @project_id
set s.scope_model_uuid = sm.scope_model_id
where s.scope_model_id is not null;

select 'Scopes with null scope_model_uuid:', count(*)
from scope
where scope_model_id is not null
  and scope.scope_model_uuid is null;

alter table scope
	drop column scope_model_id;
alter table scope
	change column scope_model_uuid scope_model_id uuid not null;

-- ============================================================
-- SCOPE_AUDIT
-- ============================================================
alter table scope_audit
	add column scope_model_uuid uuid null after scope_model_id;

update scope_audit sa
	inner join scope_model sm on sa.scope_model_id = sm.code and sm.project_id = @project_id
set sa.scope_model_uuid = sm.scope_model_id
where sa.scope_model_id is not null;

alter table scope_audit
	drop column scope_model_id;
alter table scope_audit
	change column scope_model_uuid scope_model_id uuid null;

-- ============================================================
-- EVENT
-- ============================================================
alter table event
	add column scope_model_uuid uuid null after scope_model_id,
	add column event_model_uuid uuid null after event_model_id;

update event e
	inner join scope_model sm on e.scope_model_id = sm.code and sm.project_id = @project_id
set e.scope_model_uuid = sm.scope_model_id
where e.scope_model_id is not null;

update event e
	inner join event_model em on e.event_model_id = em.code and em.project_id = @project_id
set e.event_model_uuid = em.event_model_id
where e.event_model_id is not null;

select 'Events with null scope_model_uuid:', count(*)
from event
where scope_model_id is not null
  and scope_model_uuid is null;
select 'Events with null event_model_uuid:', count(*)
from event
where event_model_id is not null
  and event_model_uuid is null;

alter table event
	drop column scope_model_id,
	drop column event_model_id;
alter table event
	change column scope_model_uuid scope_model_id uuid null,
	change column event_model_uuid event_model_id uuid null;

-- ============================================================
-- EVENT_AUDIT
-- ============================================================
alter table event_audit
	add column scope_model_uuid uuid null after scope_model_id,
	add column event_model_uuid uuid null after event_model_id;

update event_audit ea
	inner join scope_model sm on ea.scope_model_id = sm.code and sm.project_id = @project_id
set ea.scope_model_uuid = sm.scope_model_id
where ea.scope_model_id is not null;

update event_audit ea
	inner join event_model em on ea.event_model_id = em.code and em.project_id = @project_id
set ea.event_model_uuid = em.event_model_id
where ea.event_model_id is not null;

alter table event_audit
	drop column scope_model_id,
	drop column event_model_id;
alter table event_audit
	change column scope_model_uuid scope_model_id uuid null,
	change column event_model_uuid event_model_id uuid null;

-- ============================================================
-- DATASET
-- ============================================================
alter table dataset
	add column dataset_model_uuid uuid null after dataset_model_id;

update dataset d
	inner join dataset_model dm on d.dataset_model_id = dm.code and dm.project_id = @project_id
set d.dataset_model_uuid = dm.dataset_model_id
where d.dataset_model_id is not null;

select 'Datasets with null dataset_model_uuid:', count(*)
from dataset
where dataset_model_id is not null
  and dataset_model_uuid is null;

alter table dataset
	drop column dataset_model_id;
alter table dataset
	change column dataset_model_uuid dataset_model_id uuid null;

-- ============================================================
-- DATASET_AUDIT
-- ============================================================
alter table dataset_audit
	add column dataset_model_uuid uuid null after dataset_model_id;

update dataset_audit da
	inner join dataset_model dm on da.dataset_model_id = dm.code and dm.project_id = @project_id
set da.dataset_model_uuid = dm.dataset_model_id
where da.dataset_model_id is not null;

alter table dataset_audit
	drop column dataset_model_id;
alter table dataset_audit
	change column dataset_model_uuid dataset_model_id uuid null;

-- ============================================================
-- FIELD
-- ============================================================
alter table field
	add column dataset_model_uuid uuid null after dataset_model_id,
	add column field_model_uuid   uuid null after field_model_id;

update field f
	inner join dataset_model dm on f.dataset_model_id = dm.code and dm.project_id = @project_id
	inner join field_model fm on f.field_model_id = fm.code and fm.dataset_model_id = dm.dataset_model_id and
								 fm.project_id = @project_id
set f.dataset_model_uuid = dm.dataset_model_id,
	f.field_model_uuid   = fm.field_model_id
where f.dataset_model_id is not null
  and f.field_model_id is not null;

select 'Fields with null dataset_model_uuid:', count(*)
from field
where dataset_model_id is not null
  and dataset_model_uuid is null;
select 'Fields with null field_model_uuid:', count(*)
from field
where field_model_id is not null
  and field_model_uuid is null;

alter table field
	drop column dataset_model_id,
	drop column field_model_id;
alter table field
	change column dataset_model_uuid dataset_model_id uuid null,
	change column field_model_uuid field_model_id uuid null;

-- ============================================================
-- FIELD_AUDIT
-- ============================================================
alter table field_audit
	add column dataset_model_uuid uuid null after dataset_model_id,
	add column field_model_uuid   uuid null after field_model_id;

update field_audit fa
	inner join dataset_model dm on fa.dataset_model_id = dm.code and dm.project_id = @project_id
	inner join field_model fm on fa.field_model_id = fm.code and fm.dataset_model_id = dm.dataset_model_id and
								 fm.project_id = @project_id
set fa.dataset_model_uuid = dm.dataset_model_id,
	fa.field_model_uuid   = fm.field_model_id
where fa.dataset_model_id is not null
  and fa.field_model_id is not null;

alter table field_audit
	drop column dataset_model_id,
	drop column field_model_id;
alter table field_audit
	change column dataset_model_uuid dataset_model_id uuid null,
	change column field_model_uuid field_model_id uuid null;

-- ============================================================
-- FORM
-- ============================================================
alter table form
	add column form_model_uuid uuid null after form_model_id;

update form f
	inner join form_model fm on f.form_model_id = fm.code and fm.project_id = @project_id
set f.form_model_uuid = fm.form_model_id
where f.form_model_id is not null;

select 'Forms with null form_model_uuid:', count(*)
from form
where form_model_id is not null
  and form_model_uuid is null;

alter table form
	drop column form_model_id;
alter table form
	change column form_model_uuid form_model_id uuid null;

-- ============================================================
-- FORM_AUDIT
-- ============================================================
alter table form_audit
	add column form_model_uuid uuid null after form_model_id;

update form_audit fa
	inner join form_model fm on fa.form_model_id = fm.code and fm.project_id = @project_id
set fa.form_model_uuid = fm.form_model_id
where fa.form_model_id is not null;

alter table form_audit
	drop column form_model_id;
alter table form_audit
	change column form_model_uuid form_model_id uuid null;

-- ============================================================
-- WORKFLOW_STATUS
-- ============================================================
alter table workflow_status
	add column profile_uuid   uuid null after profile_id,
	add column state_uuid     uuid null after state_id,
	add column workflow_uuid  uuid null after workflow_id,
	add column action_uuid    uuid null after action_id,
	add column validator_uuid uuid null after validator_id;

update workflow_status ws
	left join profile p on ws.profile_id = p.code and p.project_id = @project_id
	left join workflow w on ws.workflow_id = w.code and w.project_id = @project_id
	left join workflow_state wst on ws.state_id = wst.code and wst.workflow_id = w.workflow_id and
									wst.project_id = @project_id
	left join workflow_action wa on ws.action_id = wa.code and wa.workflow_id = w.workflow_id and
									wa.project_id = @project_id
	left join validator v on ws.validator_id = v.code and v.project_id = @project_id
set ws.profile_uuid   = p.profile_id,
	ws.state_uuid     = wst.workflow_state_id,
	ws.workflow_uuid  = w.workflow_id,
	ws.action_uuid    = wa.workflow_action_id,
	ws.validator_uuid = v.validator_id
where ws.profile_id is not null
  and ws.workflow_id is not null
  and ws.state_id is not null
  and ws.action_id is not null
  and ws.validator_id is not null;

select 'Workflow statuses with null workflow_uuid:', count(*)
from workflow_status
where workflow_id is not null
  and workflow_uuid is null;
select 'Workflow statuses with null state_uuid:', count(*)
from workflow_status
where state_id is not null
  and state_uuid is null;

alter table workflow_status
	drop column profile_id,
	drop column state_id,
	drop column workflow_id,
	drop column action_id,
	drop column validator_id;

alter table workflow_status
	change column profile_uuid profile_id uuid null,
	change column state_uuid workflow_state_id uuid null,
	change column workflow_uuid workflow_id uuid null,
	change column action_uuid workflow_action_id uuid null,
	change column validator_uuid validator_id uuid null;

-- ============================================================
-- WORKFLOW_STATUS_AUDIT
-- ============================================================
alter table workflow_status_audit
	add column profile_uuid   uuid null after profile_id,
	add column state_uuid     uuid null after state_id,
	add column workflow_uuid  uuid null after workflow_id,
	add column action_uuid    uuid null after action_id,
	add column validator_uuid uuid null after validator_id;

update workflow_status_audit wsa
	left join profile p on wsa.profile_id = p.code and p.project_id = @project_id
	left join workflow w on wsa.workflow_id = w.code and w.project_id = @project_id
	left join workflow_state wst on wsa.state_id = wst.code and wst.workflow_id = w.workflow_id and
									wst.project_id = @project_id
	left join workflow_action wa on wsa.action_id = wa.code and wa.workflow_id = w.workflow_id and
									wa.project_id = @project_id
	left join validator v on wsa.validator_id = v.code and v.project_id = @project_id
set wsa.profile_uuid   = p.profile_id,
	wsa.state_uuid     = wst.workflow_state_id,
	wsa.workflow_uuid  = w.workflow_id,
	wsa.action_uuid    = wa.workflow_action_id,
	wsa.validator_uuid = v.validator_id
where wsa.profile_id is not null
  and wsa.workflow_id is not null
  and wsa.state_id is not null
  and wsa.action_id is not null
  and wsa.validator_id is not null;

alter table workflow_status_audit
	drop column profile_id,
	drop column state_id,
	drop column workflow_id,
	drop column action_id,
	drop column validator_id;

alter table workflow_status_audit
	change column profile_uuid profile_id uuid null,
	change column state_uuid workflow_state_id uuid null,
	change column workflow_uuid workflow_id uuid null,
	change column action_uuid workflow_action_id uuid null,
	change column validator_uuid validator_id uuid null;

-- ============================================================
-- USER_ROLE
-- ============================================================
alter table role
	add column profile_uuid uuid null after profile_id;

update role ur
	inner join profile p on ur.profile_id = p.code and p.project_id = @project_id
set ur.profile_uuid = p.profile_id
where ur.profile_id is not null;

select 'User roles with null profile_uuid:', count(*)
from role
where profile_id is not null
  and profile_uuid is null;

alter table role
	drop column profile_id;
alter table role
	change column profile_uuid profile_id uuid null;

-- ============================================================
-- USER_ROLE_AUDIT
-- ============================================================
alter table role_audit
	add column profile_uuid uuid null after profile_id;

update role_audit ura
	inner join profile p on ura.profile_id = p.code and p.project_id = @project_id
set ura.profile_uuid = p.profile_id
where ura.profile_id is not null;

alter table role_audit
	drop column profile_id;
alter table role_audit
	change column profile_uuid profile_id uuid null;

-- ============================================================
-- PAYMENT_LINE
-- ============================================================
alter table payment
	add column plan_uuid uuid null after plan_id,
	add column step_uuid uuid null after step_id;

update payment p
	left join payment_plan pp on p.plan_id = pp.code and pp.project_id = @project_id
	left join payment_step ps on p.step_id = ps.code and ps.payment_plan_id = pp.payment_plan_id and
								 ps.project_id = @project_id
set p.plan_uuid = pp.payment_plan_id,
	p.step_uuid = ps.payment_step_id
where p.plan_id is not null
  and p.step_id is not null;

alter table payment
	drop column plan_id,
	drop column step_id;
alter table payment
	change column plan_uuid payment_plan_id uuid null,
	change column step_uuid payment_step_id uuid null;

-- ============================================================
-- PAYMENT_BATCH
-- ============================================================
alter table payment_batch
	add column plan_uuid uuid null after plan_id;

update payment_batch pb
	left join payment_plan pp on pb.plan_id = pp.code and pp.project_id = @project_id
set pb.plan_uuid = pp.payment_plan_id
where pb.plan_id is not null;

alter table payment_batch
	drop column plan_id;
alter table payment_batch
	change column plan_uuid payment_plan_id uuid null;

-- ============================================================
-- RESOURCE
-- ============================================================
alter table resource
	add column category_uuid uuid null after category_id;

update resource r
	inner join resource_category rc on r.category_id = rc.code and rc.project_id = @project_id
set r.category_uuid = rc.category_id
where r.category_id is not null;

select 'Resources with null category_uuid:', count(*)
from resource
where category_id is not null
  and category_uuid is null;

alter table resource
	drop column category_id;
alter table resource
	change column category_uuid category_id uuid null;

set SQL_SAFE_UPDATES = 1;
