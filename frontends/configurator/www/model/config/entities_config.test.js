import '../../basic-tools/extension.js';

import {Action} from './entities/action.js';
import {FieldModel} from './entities/field_model.js';
import {DatasetModel} from './entities/dataset_model.js';
import {EventModel} from './entities/event_model.js';
import {EventGroup} from './entities/event_group.js';
import {Feature} from './entities/feature.js';
import {Language} from './entities/language.js';
import {Menu} from './entities/menu.js';
import {FormModel} from './entities/form_model.js';
import {PossibleValue} from './entities/possible_value.js';
import {Profile} from './entities/profile.js';
import {Rule} from './entities/rule.js';
import {RuleCondition} from './entities/rule_condition.js';
import {RuleConditionCriterion} from './entities/rule_condition_criterion.js';
import {RuleConditionList} from './entities/rule_condition_list.js';
import {RuleConstraint} from './entities/rule_constraint.js';
import {ScopeModel} from './entities/scope_model.js';
import {Study} from './entities/study.js';
import {Workflow} from './entities/workflow.js';
import {WorkflowState} from './entities/workflow_state.js';
import {create_config} from './entities_config.js';
import {Entities} from './entities.js';

export default async function test(bundle, assert) {
	//TODO remove this
	//this is still used to prototype some code on existing classes
	create_config();

	bundle.begin();

	await bundle.describe('Node#constructor', async feature => {
		const study = new Study();
		study.id = 'TEST';

		await feature.it('creates a node properly', () => {
			assert.equal(study.id, 'TEST', 'Study id is "TEST"');
			assert.equal(study.constructor, Study, 'Study constructor is Study class function');
			assert.equal(study.getEntity(), Entities.Study, 'Study entity is the "Study" entity');
		});

		const scope_model = new ScopeModel({
			id: 'PATIENT',
			shortname: {
				en: 'Patient',
				fr: 'Patient'
			}
		});

		const menu = new Menu({
			id: 'HOME',
			public: true
		});

		await feature.it('creates a node properly using parameters', () => {
			assert.equal(scope_model.id, 'PATIENT', 'Scope model id is "PATIENT"');
			assert.equal(scope_model.shortname.fr, 'Patient', 'Scope model id is "PATIENT"');
			assert.equal(scope_model.getEntity(), Entities.ScopeModel, 'Scope model entity is the "ScopeModel" entity');
			assert.ok(menu.public, 'Setting a menu public gives a public menu');
		});

		await feature.it('set default values properly', () => {
			const doc = new DatasetModel({
				id: 'PATIENT_DOCUMENT'
			});
			assert.notOk(doc.multiple, 'New dataset model is not multiple');
			assert.ok(doc.exportable, 'New dataset model is exportable');
		});

		//check all instances do not share same primary type properties values
		const study_2 = new Study();

		await feature.it('creates a node that do not share the same properties with a primary type', () => {
			assert.equal(study_2.id, undefined, 'Each instance has its own primary type properties');
			assert.notEqual(study.id, study_2.id, 'Each instance has its own primary type properties');
			study_2.id = 'OTHER_TEST';
			assert.equal(study_2.id, 'OTHER_TEST', 'Each instance has its own primary type properties');
			assert.notEqual(study.id, study_2.id, 'Each instance has its own primary type properties');
		});

		const language = new Language({
			id: 'en',
			shortname: {en: 'English'}
		});
		language.study = study;
		study.languages.push(language);

		//check all instances do not share same advanced type properties values
		await feature.it('creates node that do not share the same properties with an advanced type', () => {
			assert.notEqual(study.languages, study_2.languages, 'Each instance has its own advanced type properties');
		});
	});

	//study
	const study = new Study();
	study.id = 'TEST';

	//language
	const language_1 = new Language({
		id: 'en',
		shortname: {
			en: 'English',
			fr: 'Anglais',
			de: 'Englisch'
		}
	});
	language_1.study = study;
	study.languages.push(language_1);

	const language_2 = new Language({
		id: 'fr',
		shortname: {
			en: 'French',
			fr: 'Français',
			de: 'Französisch'
		}
	});
	language_2.study = study;
	study.languages.push(language_2);

	const language_3 = new Language({
		id: 'de',
		shortname: {
			en: 'German',
			fr: 'Allemand',
			de: 'Deutsch'
		}
	});
	language_3.study = study;
	study.languages.push(language_3);

	study.languageIds = ['en', 'fr'];

	//scope model
	const scope_model = new ScopeModel();
	scope_model.id = 'PATIENT';
	scope_model.shortname = {
		en: 'Patient',
		fr: 'Patient'
	};

	//add scope model in study
	scope_model.study = study;
	study.scopeModels.push(scope_model);

	//serialization
	/*assert.doesNotThrow(
		function() {
			assert.equal(
				JSON.stringify(scope_model),
				'{"id":"PATIENT","shortname":{"en":"Patient","fr":"Patient"},"pluralShortname":{},"longname":{},"description":{},"parents":[],"virtual":false,"workflows":[]}',
				'Scope model can be stringifed without cycle problems due to relation with study');
		},
		'Scope model can be stringified'
	);*/

	const scope_model_2 = new ScopeModel({
		id: 'CENTER',
		shortname: {
			en: 'Center',
			fr: 'Centre'
		}
	});
	scope_model_2.study = study;
	study.scopeModels.push(scope_model_2);

	//scope model relations
	scope_model.parentIds = ['CENTER'];
	scope_model.defaultParentId = 'CENTER';

	const scope_model_3 = new ScopeModel({
		id: 'COUNTRY',
		shortname: {
			en: 'Country',
			fr: 'Pays'
		}
	});
	scope_model_3.study = study;
	study.scopeModels.push(scope_model_3);

	//scope model relations
	scope_model_2.parentIds = ['COUNTRY'];

	const scope_model_4 = new ScopeModel({
		id: 'STUDY',
		shortname: {
			en: 'Study',
			fr: 'Etude'
		}
	});
	scope_model_4.study = study;
	study.scopeModels.push(scope_model_4);

	scope_model_3.parentIds = ['STUDY'];

	const scope_model_5 = new ScopeModel({
		id: 'SUBSTUDY',
		shortname: {
			en: 'Substudy',
			fr: 'Sous-étude'
		}
	});
	scope_model_5.study = study;
	study.scopeModels.push(scope_model_5);

	scope_model_5.parentIds = ['STUDY'];
	scope_model.parentIds.push('SUBSTUDY');

	//event group
	const event_group = new EventGroup({
		id: 'TREATMENTS',
		shortname: {
			en: 'Treatments',
			fr: 'Traitements'
		}
	});
	event_group.scopeModel = scope_model;
	scope_model.eventGroups.push(event_group);

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

	/*
	assert.equal(event_group.getFirstEvent().id, 'SCREENING', 'First event in event group is the event with id "SCREENING"');
	assert.equal(event_group.getLastEvent().id, 'TERMINATION_VISIT', 'Last event in event group is the event with id "SCREENING"');
	assert.event(event_4.getPreviousEvent().id, 'FOLLOW_UP_VISIT', 'Event with id "FOLLOW_UP_VISIT" is before event with id "TERMINATION_VISIT"');
	*/

	//dataset model
	const dataset_model = new DatasetModel({
		id: 'PATIENT_DOCUMENT'
	});
	dataset_model.study = study;
	study.datasetModels.push(dataset_model);

	//field model
	const field_model_1 = new FieldModel({
		id: 'GENDER',
		exportable: true
	});
	field_model_1.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_1);

	const field_model_2 = new FieldModel({
		id: 'EMPLOYMENT'
	});
	field_model_2.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_2);

	const field_model_2_possible_value_1 = new PossibleValue({
		id: 'EMPLOYED',
		shortname: {
			en: 'Employed'
		},
		specify: false
	});
	field_model_2.possibleValues.push(field_model_2_possible_value_1);
	const field_model_2_possible_value_2 = new PossibleValue({
		id: 'RETIRED',
		shortname: {
			en: 'Retired'
		},
		specify: false
	});
	field_model_2.possibleValues.push(field_model_2_possible_value_2);

	const field_model_3 = new FieldModel({
		id: 'EDUCATION'
	});
	field_model_3.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_3);

	//menu
	const menu = new Menu({
		id: 'HOME',
		public: true
	});
	menu.study = study;
	study.menus.push(menu);

	//TODO test public and private menus

	const menu_1 = new Menu({
		id: 'HELP',
		shortname: {
			en: 'Help',
			fr: 'Aide'
		}
	});
	menu_1.study = study;
	study.menus.push(menu_1);

	const menu_2 = new Menu({
		id: 'DASHBOARD',
		shortname: {
			en: 'Dashboard',
			fr: 'Tableau de bord'
		}
	});
	menu_2.study = study;
	study.menus.push(menu_2);

	const submenu_1 = new Menu({
		id: 'SUPPORT',
		shortname: {
			en: 'Support',
			fr: 'Support'
		}
	});
	submenu_1.study = study;
	submenu_1.parent = menu_1.id;
	study.menus.push(submenu_1);

	//profile
	const profile = new Profile({
		id: 'ADMIN',
		shortname: {
			en: 'Admin',
			fr: 'Admin'
		}
	});
	profile.study = study;
	study.profiles.push(profile);

	//feature
	const feature = new Feature({
		id: 'MANAGE_DELETED_DATA'
	});
	feature.study = study;
	study.features.push(feature);

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

	//workflow states
	const workflow_state_1 = new WorkflowState({
		id: 'SUBMITTED',
		shortname: {
			en: 'Submitted',
			fr: 'Soumis'
		}
	});
	workflow_state_1.workflow = workflow;
	workflow.states.push(workflow_state_1);

	const workflow_state_2 = new WorkflowState({
		id: 'REVIEWED',
		shortname: {
			en: 'Reviewed',
			fr: 'Revue'
		}
	});
	workflow_state_2.workflow = workflow;
	workflow.states.push(workflow_state_2);

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

	form_model.workflowIds.push(workflow.id);
	event_model.workflowIds.push(workflow.id);

	//rule
	const rule = new Rule({
		description: 'Set workflow status to "DONE" if current workflow status is "TODO", form model "STUDY_ENTRY" has status "REVIEWED" for workflow "REVIEW" and visit "BASELINE" has status "REVIEWED" for workflow "REVIEW"',
		message: 'It\'s done !'
	});
	rule.rulable = action;

	//constraint
	const constraint = new RuleConstraint();
	constraint.constrainable = rule;
	rule.constraint = constraint;

	//rule condition starting from VISIT entity
	//goal is to check that status of workflow "REVIEW" on event "BASELINE" is "REVIEWED"
	const rule_condition_list_visit = new RuleConditionList({
		mode: 'OR'
	});
	rule_condition_list_visit.constraint = constraint;
	constraint.conditions['VISIT'] = rule_condition_list_visit;

	//retrieve visit with id "STUDY_ENTRY"
	const rule_condition_visit = new RuleCondition({
		id: '21'
	});
	rule_condition_visit.parent = rule_condition_list_visit;
	rule_condition_list_visit.conditions.push(rule_condition_visit);

	const rule_condition_criterion_visit = new RuleConditionCriterion({
		property: 'ID',
		operator: 'EQUALS',
		values: ['BASELINE']
	});
	rule_condition_criterion_visit.condition = rule_condition_visit;
	rule_condition_visit.criterion = rule_condition_criterion_visit;

	//retrieve visit workflows
	const rule_condition_visit_workflow = new RuleCondition({
		id: '211'
	});
	rule_condition_visit_workflow.parent = rule_condition_visit;
	rule_condition_visit.conditions.push(rule_condition_visit_workflow);

	const rule_condition_criterion_visit_workflow = new RuleConditionCriterion({
		property: 'WORKFLOW'
	});
	rule_condition_criterion_visit_workflow.condition = rule_condition_visit_workflow;
	rule_condition_visit_workflow.criterion = rule_condition_criterion_visit_workflow;

	//retrieve workflow with id "REVIEW"
	const rule_condition_visit_workflow_id = new RuleCondition({
		id: '2111'
	});
	rule_condition_visit_workflow_id.parent = rule_condition_visit_workflow;
	rule_condition_visit_workflow.conditions.push(rule_condition_visit_workflow_id);

	const rule_condition_criterion_visit_workflow_id = new RuleConditionCriterion({
		property: 'ID',
		operator: 'EQUALS',
		values: ['REVIEW']
	});
	rule_condition_criterion_visit_workflow_id.condition = rule_condition_visit_workflow_id;
	rule_condition_visit_workflow_id.criterion = rule_condition_criterion_visit_workflow_id;

	//check status is "REVIEWED"
	const rule_condition_visit_workflow_id_status = new RuleCondition({
		id: '21111'
	});
	rule_condition_visit_workflow_id_status.parent = rule_condition_visit_workflow_id;
	rule_condition_visit_workflow_id.conditions.push(rule_condition_visit_workflow_id_status);

	const rule_condition_criterion_visit_workflow_id_status = new RuleConditionCriterion({
		property: 'ID',
		operator: 'EQUALS',
		values: ['REVIEWED']
	});
	rule_condition_criterion_visit_workflow_id_status.condition = rule_condition_visit_workflow_id_status;
	rule_condition_visit_workflow_id_status.criterion = rule_condition_criterion_visit_workflow_id_status;

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

	//rule action

	bundle.end();
}
