import {Study} from './study.js';
import {EventModel} from './event_model.js';
import {FormModel} from './form_model.js';
import {Workflow} from './workflow.js';
import {Action} from './action.js';
import {Rule} from './rule.js';
import {RuleConstraint} from './rule_constraint.js';
import {RuleConditionList} from './rule_condition_list.js';
import {RuleCondition} from './rule_condition.js';
import {RuleConditionCriterion} from './rule_condition_criterion.js';
import {RuleEntities} from '../rule_entities.js';
import {ScopeModel} from './scope_model.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('RuleCondition#getConstraint, RuleCondition#getRuleEntity and RuleCondition#getResults', async feature => {
		await feature.it('retrieve the entity and the results properly', () => {

			//study
			const study = new Study();
			study.id = 'TEST';

			//scope model
			const scope_model = new ScopeModel();
			scope_model.id = 'PATIENT';
			scope_model.study = study;
			study.scopeModels.push(scope_model);

			//event model
			const event_model = new EventModel({
				id: 'BASELINE',
				deadline: 0,
				deadlineUnit: 'DAYS'
			});
			event_model.scopeModel = scope_model;
			scope_model.eventModels.push(event_model);

			const event_model_2 = new EventModel({
				id: 'SCREENING',
				deadline: 0,
				deadlineUnit: 'DAYS'
			});
			event_model_2.scopeModel = scope_model;
			scope_model.eventModels.push(event_model_2);

			const event_model_3 = new EventModel({
				id: 'FOLLOW_UP_VISIT',
				deadline: 6,
				deadlineUnit: 'MONTHS'
			});
			event_model_3.scopeModel = scope_model;
			scope_model.eventModels.push(event_model_3);

			const event_model_4 = new EventModel({
				id: 'TERMINATION_VISIT',
				deadline: 12,
				deadlineUnit: 'MONTHS'
			});
			event_model_4.scopeModel = scope_model;
			scope_model.eventModels.push(event_model_4);

			//form model
			const form_model = new FormModel({
				id: 'STUDY_ENTRY',
				shortname: {
					en: 'Study entry',
					fr: 'Entrée dans l\'étude'
				}
			});
			form_model.study = study;
			study.formModels.push(form_model);

			//workflow
			const workflow = new Workflow({
				id: 'REVIEW',
				shortname: {
					en: 'Review',
					fr: 'Revision'
				}
			});
			workflow.study = study;
			study.workflows.push(workflow);
			event_model.workflowIds.push(workflow.id);
			form_model.workflowIds.push(workflow.id);

			//action
			const action = new Action({
				id: 'DO',
				shortname: {
					en: 'Do',
					fr: 'Faire'
				}
			});
			action.workflow = workflow;
			workflow.actions.push(action);

			//rule
			const rule = new Rule({
				description: 'Set workflow status to "DONE" if current workflow status is "TODO", form model "STUDY_ENTRY" has status "REVIEWED" for workflow "REVIEW" and event "BASELINE" has status "REVIEWED" for workflow "REVIEW"',
				message: 'It\'s done !'
			});
			rule.rulable = action;

			//constraint
			const constraint = new RuleConstraint();
			constraint.constrainable = rule;
			rule.constraint = constraint;

			//rule condition starting from EVENT entity
			//goal is to check that status of workflow "REVIEW" on event "BASELINE" is "REVIEWED"
			const rule_condition_list_event = new RuleConditionList({
				mode: 'OR'
			});
			rule_condition_list_event.constraint = constraint;
			constraint.conditions['EVENT'] = rule_condition_list_event;

			//retrieve event with id "STUDY_ENTRY"
			const rule_condition_event = new RuleCondition({
				id: '21'
			});
			rule_condition_event.parent = rule_condition_list_event;
			rule_condition_list_event.conditions.push(rule_condition_event);

			const rule_condition_criterion_event = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['BASELINE']
			});
			rule_condition_criterion_event.condition = rule_condition_event;
			rule_condition_event.criterion = rule_condition_criterion_event;

			assert.equal(rule_condition_event.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_event.getRuleEntity(), RuleEntities.EVENT, 'Entity of rule condition on event is "EVENT"');
			assert.equal(rule_condition_event.getResults().length, 1, 'There is 1 result for rule condition on event');
			assert.equal(rule_condition_event.getResults()[0].id, 'BASELINE', 'First result for rule condition on event is first event in the study');

			//retrieve event workflows
			const rule_condition_event_workflow = new RuleCondition({
				id: '211'
			});
			rule_condition_event_workflow.parent = rule_condition_event;
			rule_condition_event.conditions.push(rule_condition_event_workflow);

			const rule_condition_criterion_event_workflow = new RuleConditionCriterion({
				property: 'WORKFLOW'
			});
			rule_condition_criterion_event_workflow.condition = rule_condition_event_workflow;
			rule_condition_event_workflow.criterion = rule_condition_criterion_event_workflow;

			assert.equal(rule_condition_event_workflow.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_event_workflow.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_event_workflow.getResults().length, 1, 'There is 1 result for rule condition on workflow');
			assert.equal(rule_condition_event_workflow.getResults()[0].id, 'REVIEW', 'First result for rule condition on workflow is the only workflow in the study');

			//retrieve workflow with id "REVIEW"
			const rule_condition_event_workflow_id = new RuleCondition({
				id: '2111'
			});
			rule_condition_event_workflow_id.parent = rule_condition_event_workflow;
			rule_condition_event_workflow.conditions.push(rule_condition_event_workflow_id);

			const rule_condition_criterion_event_workflow_id = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['REVIEW']
			});
			rule_condition_criterion_event_workflow_id.condition = rule_condition_event_workflow_id;
			rule_condition_event_workflow_id.criterion = rule_condition_criterion_event_workflow_id;

			assert.equal(rule_condition_event_workflow_id.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_event_workflow_id.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_event_workflow_id.getResults().length, 1, 'There is 1 result for rule condition on workflow');
			assert.equal(rule_condition_event_workflow_id.getResults()[0].id, 'REVIEW', 'First result for rule condition on workflow is the only workflow in the study');

			//check status is "REVIEWED"
			const rule_condition_event_workflow_id_status = new RuleCondition({
				id: '21111'
			});
			rule_condition_event_workflow_id_status.parent = rule_condition_event_workflow_id;
			rule_condition_event_workflow_id.conditions.push(rule_condition_event_workflow_id_status);

			const rule_condition_criterion_event_workflow_id_status = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['REVIEWED']
			});
			rule_condition_criterion_event_workflow_id_status.condition = rule_condition_event_workflow_id_status;
			rule_condition_event_workflow_id_status.criterion = rule_condition_criterion_event_workflow_id_status;

			assert.equal(rule_condition_event_workflow_id_status.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_event_workflow_id_status.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_event_workflow_id_status.getResults().length, 0, 'There is 0 result for rule condition on workflow');

			//rule condition starting from WORKFLOW entity
			//goal is to check that status of the selected workflow is "TODO"
			const rule_condition_list_workflow = new RuleConditionList({
				mode: 'OR'
			});
			rule_condition_list_workflow.constraint = constraint;
			constraint.conditions['WORKFLOW'] = rule_condition_list_workflow;

			//check status is "TODO"
			const rule_condition_workflow = new RuleCondition({
				id: '61'
			});
			rule_condition_workflow.parent = rule_condition_list_workflow;
			rule_condition_list_workflow.conditions.push(rule_condition_workflow);

			assert.equal(rule_condition_workflow.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');

			//rule condition criterion
			const rule_condition_criterion_workflow = new RuleConditionCriterion({
				property: 'STATUS',
				operator: 'EQUALS',
				values: ['TODO']
			});
			rule_condition_criterion_workflow.condition = rule_condition_workflow;
			rule_condition_workflow.criterion = rule_condition_criterion_workflow;

			//rule condition starting from FORM entity
			//goal is to check that status of workflow "REVIEW" on form model "STUDY_ENTRY" is "REVIEWED"
			const rule_condition_list_form_model = new RuleConditionList({
				mode: 'OR'
			});
			rule_condition_list_form_model.constraint = constraint;
			constraint.conditions['FORM'] = rule_condition_list_form_model;

			//retrieve form model with id "STUDY_ENTRY"
			const rule_condition_form_model = new RuleCondition({
				id: '51'
			});
			rule_condition_form_model.parent = rule_condition_list_form_model;
			rule_condition_list_form_model.conditions.push(rule_condition_form_model);

			const rule_condition_criterion_form_model = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['STUDY_ENTRY']
			});
			rule_condition_criterion_form_model.condition = rule_condition_form_model;
			rule_condition_form_model.criterion = rule_condition_criterion_form_model;

			assert.equal(rule_condition_form_model.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_form_model.getRuleEntity(), RuleEntities.FORM, 'Entity of rule condition on form model is "FORM"');
			assert.equal(rule_condition_form_model.getResults().length, 1, 'There is 1 result for rule condition on form model');
			assert.equal(rule_condition_form_model.getResults()[0].id, 'STUDY_ENTRY', 'First result for rule condition on form model is the only form model in the study');

			//retrieve form model workflows
			const rule_condition_form_model_workflow = new RuleCondition({
				id: '511'
			});
			rule_condition_form_model_workflow.parent = rule_condition_form_model;
			rule_condition_form_model.conditions.push(rule_condition_form_model_workflow);

			const rule_condition_criterion_form_model_workflow = new RuleConditionCriterion({
				property: 'WORKFLOW'
			});
			rule_condition_criterion_form_model_workflow.condition = rule_condition_form_model_workflow;
			rule_condition_form_model_workflow.criterion = rule_condition_criterion_form_model_workflow;

			assert.equal(rule_condition_form_model_workflow.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_form_model_workflow.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_form_model_workflow.getResults().length, 1, 'There is 1 result for rule condition on workflow');
			assert.equal(rule_condition_form_model_workflow.getResults()[0].id, 'REVIEW', 'First result for rule condition on workflow is the only workflow in the study');

			//retrieve workflow with id "REVIEW"
			const rule_condition_form_model_workflow_id = new RuleCondition({
				id: '5111'
			});
			rule_condition_form_model_workflow_id.parent = rule_condition_form_model_workflow;
			rule_condition_form_model_workflow.conditions.push(rule_condition_form_model_workflow_id);

			const rule_condition_criterion_form_model_workflow_id = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['REVIEW']
			});
			rule_condition_criterion_form_model_workflow_id.condition = rule_condition_form_model_workflow_id;
			rule_condition_form_model_workflow_id.criterion = rule_condition_criterion_form_model_workflow_id;

			assert.equal(rule_condition_form_model_workflow_id.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_form_model_workflow_id.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_form_model_workflow_id.getResults().length, 1, 'There is 1 result for rule condition on workflow');
			assert.equal(rule_condition_form_model_workflow_id.getResults()[0].id, 'REVIEW', 'First result for rule condition on workflow is the only workflow in the study');

			//check status is "REVIEWED"
			const rule_condition_form_model_workflow_id_status = new RuleCondition({
				id: '51111'
			});
			rule_condition_form_model_workflow_id_status.parent = rule_condition_form_model_workflow_id;
			rule_condition_form_model_workflow_id.conditions.push(rule_condition_form_model_workflow_id_status);

			const rule_condition_criterion_form_model_workflow_id_status = new RuleConditionCriterion({
				property: 'ID',
				operator: 'EQUALS',
				values: ['REVIEWED']
			});
			rule_condition_criterion_form_model_workflow_id_status.condition = rule_condition_form_model_workflow_id_status;
			rule_condition_form_model_workflow_id_status.criterion = rule_condition_criterion_form_model_workflow_id_status;

			assert.equal(rule_condition_form_model_workflow_id_status.getConstraint(), constraint, 'Rule condition constraint gives the good constraint');
			assert.equal(rule_condition_form_model_workflow_id_status.getRuleEntity(), RuleEntities.WORKFLOW, 'Entity of rule condition on workflow is "WORKFLOW"');
			assert.equal(rule_condition_form_model_workflow_id_status.getResults().length, 0, 'There is 0 result for rule condition on workflow');
		});
	});

	bundle.end();
}
