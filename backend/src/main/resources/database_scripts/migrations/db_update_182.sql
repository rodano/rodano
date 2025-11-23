/* ==========================================================
   POPULATE NEW TABLES WITH DATA FROM EXISTING TABLES
   ========================================================== */

set @project_id = (select project_id
				   from project
				   where code = 'TEST'
				   limit 1);

-- ============================================================
-- SCOPE_MODEL
-- ============================================================
insert into scope_model (scope_model_id, project_id, code)
select distinct uuid() as scope_model_id, @project_id as project_id, s.scope_model_id as code
from scope s
where s.scope_model_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- EVENT_MODEL
-- ============================================================
insert into event_model (event_model_id, project_id, code, scope_model_id)
select distinct uuid() as event_model_id, @project_id as project_id, e.event_model_id as code, sm.scope_model_id
from event e
		 inner join scope_model sm on e.scope_model_id = sm.code and sm.project_id = @project_id
where e.event_model_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- DATASET_MODEL
-- ============================================================
insert into dataset_model (dataset_model_id, project_id, code)
select distinct uuid() as dataset_model_id, @project_id as project_id, d.dataset_model_id as code
from dataset d
where d.dataset_model_id is not null
union
select distinct uuid() as dataset_model_id, @project_id as project_id, da.dataset_model_id as code
from dataset_audit da
where da.dataset_model_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- FIELD_MODEL
-- ============================================================
insert into field_model (field_model_id, project_id, dataset_model_id, code)
select distinct uuid() as field_model_id, @project_id as project_id, dm.dataset_model_id, f.field_model_id as code
from field f
		 inner join dataset_model dm on f.dataset_model_id = dm.code and dm.project_id = @project_id
where f.field_model_id is not null
union
select distinct uuid() as field_model_id, @project_id as project_id, dm.dataset_model_id, fa.field_model_id as code
from field_audit fa
		 inner join dataset_model dm on fa.dataset_model_id = dm.code and dm.project_id = @project_id
where fa.field_model_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- FORM_MODEL
-- ============================================================
insert into form_model (form_model_id, project_id, code)
select distinct uuid() as form_model_id, @project_id as project_id, f.form_model_id as code
from form f
where f.form_model_id is not null
union
select distinct uuid() as form_model_id, @project_id as project_id, fa.form_model_id as code
from form_audit fa
where fa.form_model_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- WORKFLOW
-- ============================================================
insert into workflow (workflow_id, project_id, code)
select distinct uuid() as workflow_id, @project_id as project_id, ws.workflow_id as code
from workflow_status ws
where ws.workflow_id is not null
union
select distinct uuid() as workflow_id, @project_id as project_id, wsa.workflow_id as code
from workflow_status_audit wsa
where wsa.workflow_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- WORKFLOW_STATE
-- ============================================================
insert into workflow_state (workflow_state_id, project_id, workflow_id, code)
select distinct uuid() as workflow_state_id, @project_id as project_id, w.workflow_id, ws.state_id as code
from workflow_status ws
		 inner join workflow w on ws.workflow_id = w.code and w.project_id = @project_id
where ws.state_id is not null
union
select distinct uuid() as workflow_state_id, @project_id as project_id, w.workflow_id, wsa.state_id as code
from workflow_status_audit wsa
		 inner join workflow w on wsa.workflow_id = w.code and w.project_id = @project_id
where wsa.state_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- WORKFLOW_ACTION
-- ============================================================
insert into workflow_action (workflow_action_id, project_id, workflow_id, code)
select distinct uuid() as workflow_action_id, @project_id as project_id, w.workflow_id, ws.action_id as code
from workflow_status ws
		 inner join workflow w on ws.workflow_id = w.code and w.project_id = @project_id
where ws.action_id is not null
union
select distinct uuid() as workflow_action_id, @project_id as project_id, w.workflow_id, wsa.action_id as code
from workflow_status_audit wsa
		 inner join workflow w on wsa.workflow_id = w.code and w.project_id = @project_id
where wsa.action_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- VALIDATOR
-- ============================================================
insert into validator (validator_id, project_id, code)
select distinct uuid() as validator_id, @project_id as project_id, ws.validator_id as code
from workflow_status ws
where ws.validator_id is not null
union
select distinct uuid() as validator_id, @project_id as project_id, wsa.validator_id as code
from workflow_status_audit wsa
where wsa.validator_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- PROFILE
-- ============================================================
insert into profile (profile_id, project_id, code)
select distinct uuid() as profile_id, @project_id as project_id, r.profile_id as code
from role r
where r.profile_id is not null
union
select distinct uuid() as profile_id, @project_id as project_id, ra.profile_id as code
from role_audit ra
where ra.profile_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- PAYMENT_PLAN
-- ============================================================
insert into payment_plan (payment_plan_id, project_id, code)
select distinct uuid() as payment_plan_id, @project_id as project_id, p.plan_id as code
from payment p
where p.plan_id is not null
union
select distinct uuid() as payment_plan_id, @project_id as project_id, pb.plan_id as code
from payment_batch pb
where pb.plan_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- PAYMENT_PLAN
-- ============================================================
insert into payment_step (payment_step_id, project_id, payment_plan_id, code)
select distinct uuid() as payment_step_id, @project_id as project_id, pp.payment_plan_id, p.step_id as code
from payment p
		 inner join payment_plan pp on p.plan_id = pp.code and pp.project_id = @project_id
where p.step_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- RESOURCE_CATEGORY
-- ============================================================
insert into resource_category (category_id, project_id, code)
select distinct uuid() as category_id, @project_id as project_id, r.category_id as code
from resource r
where r.category_id is not null
on duplicate key update code = values(code);

-- ============================================================
-- VERIFICATION
-- ============================================================
select 'Scope Models:', count(*) from scope_model where project_id = @project_id;
select 'Event Models:', count(*) from event_model where project_id = @project_id;
select 'Dataset Models:', count(*) from dataset_model where project_id = @project_id;
select 'Field Models:', count(*) from field_model where project_id = @project_id;
select 'Form Models:', count(*) from form_model where project_id = @project_id;
select 'Workflows:', count(*) from workflow where project_id = @project_id;
select 'Workflows States:', count(*) from workflow_state where project_id = @project_id;
select 'Workflows Actions:', count(*) from workflow_action where project_id = @project_id;
select 'Validators:', count(*) from validator where project_id = @project_id;
select 'Profiles:', count(*) from profile where project_id = @project_id;
select 'Payment Plans:', count(*) from payment_plan where project_id = @project_id;
select 'Payment Steps:', count(*) from payment_step where project_id = @project_id;
select 'Resource Categories:', count(*) from resource_category where project_id = @project_id;
