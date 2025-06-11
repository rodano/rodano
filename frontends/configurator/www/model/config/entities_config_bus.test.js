import '../../basic-tools/extension.js';

import {bus} from './entities_hooks.js';
import {Study} from './entities/study.js';
import {Language} from './entities/language.js';
import {Action} from './entities/action.js';
import {FieldModel} from './entities/field_model.js';
import {Cell} from './entities/cell.js';
import {DatasetModel} from './entities/dataset_model.js';
import {Entities} from './entities.js';
import {EventModel} from './entities/event_model.js';
import {EventGroup} from './entities/event_group.js';
import {Feature} from './entities/feature.js';
import {Layout} from './entities/layout.js';
import {Line} from './entities/line.js';
import {Menu} from './entities/menu.js';
import {FormModel} from './entities/form_model.js';
import {PaymentPlan} from './entities/payment_plan.js';
import {PaymentStep} from './entities/payment_step.js';
import {PrivacyPolicy} from './entities/privacy_policy.js';
import {Profile} from './entities/profile.js';
import {ProfileRight} from './entities/profile_right.js';
import {ResourceCategory} from './entities/resource_category.js';
import {Right} from './entities/right.js';
import {ScopeModel} from './entities/scope_model.js';
import {TimelineGraph} from './entities/timeline_graph.js';
import {TimelineGraphSection} from './entities/timeline_graph_section.js';
import {Validator} from './entities/validator.js';
import {VisibilityCriteria} from './entities/visibility_criteria.js';
import {Workflow} from './entities/workflow.js';
import {WorkflowState} from './entities/workflow_state.js';
import {WorkflowStatesSelector} from './entities/workflow_states_selector.js';
import {WorkflowWidget} from './entities/workflow_widget.js';

export default async function test(bundle, assert) {
	bundle.begin();

	await bundle.describe('entities listeners methods', async feature => {
		//check good listeners have been set to study
		await feature.it('registers good listeners on study', () => {
			const study = new Study({id: 'TEST'});
			assert.ok(Function.isFunction(study.onDeleteLanguage), 'Study is a displayable therefore it has a "onDeleteLanguage" method');
			assert.ok(Function.isFunction(study.onChangeLanguageId), 'Study is a displayable therefore it has a "onChangeLanguageId" method');
		});

		//check good listeners have been set to language
		await feature.it('registers good listeners on language', () => {
			const language = new Language({id: 'en'});
			assert.ok(Function.isFunction(language.onDeleteLanguage), 'Language is a displayable therefore it has a "onDeleteLanguage" method');
			assert.ok(Function.isFunction(language.onChangeLanguageId), 'Language is a displayable therefore it has a "onChangeLanguageId" method');
		});
	});

	const global_listener = {
		deletions: [],
		modifications: [],
		onDelete: function(event) {
			this.deletions.push(event);
		},
		onChange: function(event) {
			this.modifications.push(event);
		}
	};
	bus.register(global_listener);

	//build a configuration
	//study
	const study = new Study({id: 'TEST'});
	study.shortname = {
		en: 'Test study',
		fr: 'Etude de test'
	};

	//language
	const language_1 = new Language({id: 'en'});
	language_1.shortname = {
		en: 'English',
	};
	language_1.study = study;
	study.languages.push(language_1);

	const language_2 = new Language({id: 'fr'});
	language_2.shortname = {
		en: 'French',
		fr: 'Français'
	};
	language_2.study = study;
	study.languages.push(language_2);

	const language_3 = new Language({id: 'de'});
	language_3.shortname = {
		en: 'German',
		fr: 'Allemand',
		de: 'Deutsch'
	};
	language_3.study = study;
	study.languages.push(language_3);

	assert.equal(global_listener.deletions.length, 0, '0 deletion has occurred');
	assert.equal(global_listener.modifications.length, 7, '7 modifications have occurred (2 for 3 languages and 1 for study)');

	//complete languages
	language_1.shortname.fr = 'Anglais';
	language_1.shortname.de = 'Englisch';
	language_2.shortname.de = 'Französisch';

	//feature
	const feature = new Feature({id: 'MANAGE_DELETED_DATA'});
	feature.study = study;
	study.features.push(feature);

	//profile
	const profile = new Profile({id: 'ADMIN'});
	profile.shortname = {
		en: 'Admin',
		fr: 'Admin'
	};
	profile.study = study;
	study.profiles.push(profile);
	profile.assignNode(feature);

	const profile_2 = new Profile({id: 'CRA'});
	profile_2.shortname = {
		en: 'CRA',
		fr: 'CRA'
	};
	profile_2.study = study;
	study.profiles.push(profile_2);
	profile.assignRightNode(profile_2, 'WRITE');

	//workflow
	const workflow = new Workflow({id: 'DATA_MANAGEMENT'});
	workflow.study = study;
	study.workflows.push(workflow);
	profile.grantedWorkflowIds[workflow.id] = new Right({right: true});

	const workflow_2 = new Workflow({id: 'SDV'});
	workflow_2.study = study;
	study.workflows.push(workflow_2);
	profile.grantedWorkflowIds[workflow_2.id] = new Right({right: true});

	const workflow_3 = new Workflow({id: 'QUERY'});
	workflow_3.study = study;
	study.workflows.push(workflow_3);
	profile.grantedWorkflowIds[workflow_3.id] = new Right({right: true});

	const workflow_4 = new Workflow({id: 'PROTOCOL_DEVIATION'});
	workflow_4.study = study;
	study.workflows.push(workflow_4);
	profile.grantedWorkflowIds[workflow_4.id] = new Right({right: true});

	//workflow widget
	const workflow_widget = new WorkflowWidget({id: 'SDV_WIDGET'});
	const workflow_widget_selector = new WorkflowStatesSelector({workflowId: 'SDV'});
	workflow_widget.workflowStatesSelectors = [workflow_widget_selector];
	workflow_widget.study = study;
	study.workflowWidgets.push(workflow_widget);

	//state
	const state = new WorkflowState({id: 'REVIEWED'});
	state.workflow = workflow;
	workflow.states.push(state);

	//action
	const action = new Action({id: 'REVIEW'});
	action.workflow = workflow;
	workflow.actions.push(action);

	state.possibleActionIds.push(action.id);

	const profile_right = new ProfileRight();
	profile_right.system = true;
	profile_right.profileIds = ['ADMIN', 'CRA'];
	profile.grantedWorkflowIds[action.workflow.id].childRights[action.id] = profile_right;

	//scope model
	const scope_model = new ScopeModel({id: 'PATIENT'});
	scope_model.shortname = {
		en: 'Patient',
		fr: 'Patient'
	};
	scope_model.study = study;
	study.scopeModels.push(scope_model);

	const scope_model_2 = new ScopeModel({id: 'CENTER'});
	scope_model_2.shortname = {
		en: 'Center',
		fr: 'Centre'
	};
	scope_model_2.study = study;
	study.scopeModels.push(scope_model_2);
	scope_model.parentIds = ['CENTER'];

	const scope_model_3 = new ScopeModel({id: 'SUBSTUDY'});
	scope_model_3.shortname = {
		en: 'Substudy',
		fr: 'Sous-étude'
	};
	scope_model_3.study = study;
	study.scopeModels.push(scope_model_3);
	scope_model.parentIds.push(scope_model_3.id);
	profile.assignRightNode(scope_model_3, 'WRITE');

	//event group
	const event_group = new EventGroup({id: 'TREATMENTS'});
	event_group.shortname = {
		en: 'Treatments',
		fr: 'Traitements'
	};
	event_group.scopeModel = scope_model;
	scope_model.eventGroups.push(event_group);

	//event model
	const event_model = new EventModel({id: 'BASELINE'});
	event_model.scopeModel = scope_model;
	scope_model.eventModels.push(event_model);
	assert.equal(scope_model.getEventModel('BASELINE'), event_model, 'Retrieve event model with id "BASELINE" in scope model gives the good event model');

	profile.assignRightNode(event_model, 'WRITE');

	const event_model_2 = new EventModel({id: 'SCREENING'});
	event_model_2.scopeModel = scope_model;
	scope_model.eventModels.push(event_model_2);

	profile.assignRightNode(event_model_2, 'WRITE');

	//dataset model
	const dataset_model = new DatasetModel({id: 'PATIENT_DOCUMENT'});
	dataset_model.study = study;
	study.datasetModels.push(dataset_model);

	event_model.datasetModelIds.push(dataset_model.id);
	profile.assignRightNode(dataset_model, 'WRITE');

	const dataset_model_2 = new DatasetModel({id: 'VISIT_DOCUMENT'});
	dataset_model_2.study = study;
	study.datasetModels.push(dataset_model_2);

	event_model.datasetModelIds.push(dataset_model_2.id);
	profile.assignRightNode(dataset_model_2, 'WRITE');

	const dataset_model_3 = new DatasetModel({id: 'TREATMENT'});
	dataset_model_3.study = study;
	study.datasetModels.push(dataset_model_3);

	event_model.datasetModelIds.push(dataset_model_3.id);

	//field model
	const field_model_1 = new FieldModel({id: 'GENDER'});
	field_model_1.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_1);

	const field_model_2 = new FieldModel({id: 'EMPLOYMENT'});
	field_model_2.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_2);

	const field_model_3 = new FieldModel({id: 'EDUCATION'});
	field_model_3.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model_3);

	const field_model_4 = new FieldModel({id: 'EDSS'});
	field_model_4.datasetModel = dataset_model_2;
	dataset_model_2.fieldModels.push(field_model_4);

	const field_model_5 = new FieldModel({id: 'AMBULATION'});
	field_model_5.datasetModel = dataset_model_2;
	dataset_model_2.fieldModels.push(field_model_5);

	//validator
	const validator = new Validator({id: 'REQUIRED'});
	validator.study = study;
	study.validators.push(validator);

	const validator_2 = new Validator({id: 'MUST_BE_AFTER_STUDY_START_DATE'});
	validator_2.study = study;
	study.validators.push(validator_2);

	field_model_1.validatorIds.push(validator.id);

	//form model
	const form_model = new FormModel({id: 'STUDY_ENTRY'});
	form_model.study = study;
	study.formModels.push(form_model);
	event_model.formModelIds.push(form_model.id);
	profile.assignRightNode(form_model, 'WRITE');

	const form_model_2 = new FormModel({id: 'EDSS'});
	form_model_2.study = study;
	study.formModels.push(form_model_2);
	event_model.formModelIds.push(form_model_2.id);
	profile.assignRightNode(form_model_2, 'WRITE');

	//layout
	const layout = new Layout({id: 'FIRST_LAYOUT'});
	layout.formModel = form_model;
	form_model.layouts.push(layout);

	//line
	const line = new Line();
	line.layout = layout;
	layout.lines.push(line);

	//cell
	const cell_1 = new Cell({id: 'GENDER_CELL'});
	cell_1.datasetModelId = 'PATIENT_DOCUMENT';
	cell_1.fieldModelId = 'GENDER';
	cell_1.line = line;
	line.cells.push(cell_1);

	const cell_2 = new Cell({id: 'EMPLOYMENT_CELL'});
	cell_2.datasetModelId = 'PATIENT_DOCUMENT';
	cell_2.fieldModelId = 'EMPLOYMENT';
	cell_2.line = line;
	line.cells.push(cell_2);

	//layout
	const layout_2 = new Layout({id: 'SECOND_LAYOUT'});
	layout_2.formModel = form_model;
	form_model.layouts.push(layout_2);

	//visibility criteria
	const visibility_criteria = new VisibilityCriteria();
	visibility_criteria.targetLayoutIds = ['SECOND_LAYOUT'];
	visibility_criteria.cell = cell_2;
	cell_2.visibilityCriteria.push(visibility_criteria);

	//layout
	const layout_3 = new Layout({id: 'TREATMENTS_LAYOUT'});
	layout_3.formModel = form_model;
	layout_3.type = 'MULTIPLE';
	layout_3.datasetModelId = dataset_model_3.id;
	form_model.layouts.push(layout_3);

	//payment plan
	const payment_plan = new PaymentPlan({id: 'VISIT_PLAN'});
	payment_plan.study = study;
	study.paymentPlans.push(payment_plan);

	//payment step
	const payment_step = new PaymentStep({id: 'VISIT_PLAN_FIRST_STEP'});
	payment_step.paymentPlan = payment_plan;
	payment_plan.steps.push(payment_step);

	//menu
	const menu = new Menu({id: 'HOME'});
	menu.public = true;
	menu.study = study;
	study.menus.push(menu);

	const menu_1 = new Menu({id: 'HELP'});
	menu_1.shortname = {
		en: 'Help',
		fr: 'Aide'
	};
	menu_1.study = study;
	study.menus.push(menu_1);
	profile.assignNode(menu_1);

	const menu_2 = new Menu({id: 'DASHBOARD'});
	menu_2.shortname = {
		en: 'Dashboard',
		fr: 'Tableau de bord'
	};
	menu_2.study = study;
	study.menus.push(menu_2);

	profile.assignNode(menu_2);

	//submenu
	const submenu_1 = new Menu({id: 'CONTACT'});
	submenu_1.shortname = {
		en: 'Contact',
		fr: 'Contact'
	};
	submenu_1.study = study;
	submenu_1.parent = menu_1;
	menu_1.submenus.push(submenu_1);

	profile.assignNode(submenu_1);

	//resource category
	const resource_category = new ResourceCategory({id: 'SAMPLE'});
	resource_category.study = study;
	study.resourceCategories.push(resource_category);

	//privacy policy
	const privacy_policy = new PrivacyPolicy({id: 'EULA'});
	privacy_policy.study = study;
	study.privacyPolicies.push(privacy_policy);

	//timeline graph
	const timeline_graph = new TimelineGraph({id: 'PATIENT_OVERVIEW'});
	timeline_graph.study = study;
	study.timelineGraphs.push(timeline_graph);

	const timeline_graph_section = new TimelineGraphSection({id: 'EDSS'});
	timeline_graph_section.type = 'LINE';
	timeline_graph_section.label = {en: 'EDSS'};
	timeline_graph_section.datasetModelId = 'VISIT_DOCUMENT';
	timeline_graph_section.valueFieldModelId = 'EDSS';
	timeline_graph_section.timelineGraph = timeline_graph;
	timeline_graph.sections.push(timeline_graph_section);

	//test configuration

	await bundle.describe('Feature', async bundle_feature => {
		//change feature id
		await bundle_feature.it('updates profiles when a feature is updated (FS_FEATURE_003)', () => {
			assert.ok(profile.isAssigned(Entities.Feature, 'MANAGE_DELETED_DATA'), 'Profile "ADMIN" contains a feature with id "MANAGE_DELETED_DATA"');
			assert.ok(profile.isAssignedNode(feature), 'Profile "ADMIN" contains feature "MANAGE_DELETED_DATA"');
			feature.id = 'TESTING';
			assert.ok(profile.isAssignedNode(feature), 'Profile "ADMIN" still contains feature "MANAGE_DELETED_DATA" after id of this feature has been changed');
			assert.notOk(profile.isAssigned(Entities.Feature, 'MANAGE_DELETED_DATA'), 'Profile "ADMIN" no more contains a feature with id "MANAGE_DELETED_DATA" after its id of this feature has been changed');
			assert.ok(profile.isAssigned(Entities.Feature, 'TESTING'), 'Profile "ADMIN" contains feature with id "TESTING"');
		});

		//delete feature
		await bundle_feature.it('deletes feature properly and updates profiles when a feature is deleted (FS_FEATURE_004, FS_FEATURE_005)', () => {
			assert.equal(study.features.length, 1, 'There is 1 feature in study');
			feature['delete']();
			assert.ok(study.features.isEmpty(), 'There is no feature in study after the only feature has been deleted');

			assert.notOk(profile.isAssignedNode(feature), 'There is no more feature "MANAGE_DELETED_DATA" for profile "ADMIN" after this feature has been deleted');
			assert.notOk(profile.isAssigned(Entities.Feature, 'TESTING'), 'There is no more feature with id "TESTING" for profile "ADMIN" after this feature has been deleted');
		});
	});

	await bundle.describe('Profile', async feature => {
		//change profile id
		await feature.it('updates other profiles when a profile is updated (FS_PROFILE_003)', () => {
			profile_2.id = 'CRO';
			assert.doesThrow(() => study.getProfile('CRA'), undefined, 'There is no more profile with id "CRA" in study');
			assert.doesNotThrow(() => study.getProfile('CRO'), 'There is a profile with id "CRO" in study');

			assert.ok(profile.isAssignedRight(Entities.Profile, 'CRO', 'WRITE'), 'Profile with id "CRO" has been assigned to profile with id "ADMIN"');
			assert.notOk(profile.isAssignedRight(Entities.Profile, 'CRA', 'WRITE'), 'Profile with id "CRA" is no more assigned to profile with id "ADMIN"');
			assert.ok(profile.grantedProfileIdRights.hasOwnProperty('CRO'), 'Profile with id "ADMIN" contains profile with id "CRO"');
			assert.notOk(profile.grantedProfileIdRights.hasOwnProperty('CRA'), 'Profile with id "ADMIN" does not contain profile with id "CRO"');
		});

		//delete profile
		await feature.it('deletes profile properly and updates other profiles when a profile is deleted (FS_PROFILE_004, FS_PROFILE_005)', () => {
			assert.equal(study.profiles.length, 2, 'There is 2 profiles in study');
			profile_2['delete']();
			assert.equal(study.profiles.length, 1, 'There is 1 profile in study after 1 profile has been deleted');
			assert.doesThrow(() => study.getProfile('CRO'), undefined, 'Profile with id "CRO" has been deleted from study');

			assert.notOk(profile.isAssignedRight(Entities.Profile, 'CRO', 'WRITE'), 'Profile with id "CRO" is no more assigned to profile with id "ADMIN"');
			assert.notOk(profile.grantedProfileIdRights.hasOwnProperty('CRO'), 'Profile with id "ADMIN" does not contains profile with id "CRO"');
		});
	});

	await bundle.describe('Workflow', async feature => {
		//change workflow id
		await feature.it('updates profiles and workflow widgets when a workflow is updated (FS_WORKFLOW_003, FS_WORKFLOW_020)', () => {
			workflow_2.id = 'DATA_VERIFICATION';
			assert.notOk(profile.grantedWorkflowIds.hasOwnProperty('SDV'), 'Profile no more contains a workflow with id "SDV"');
			assert.ok(profile.grantedWorkflowIds.hasOwnProperty('DATA_VERIFICATION'), 'Profile contains workflow with id "DATA_VERIFICATION"');
			assert.equal(workflow_widget.workflowStatesSelectors[0].workflowId, 'DATA_VERIFICATION', 'Workflow widget is updated when the id of its associated workflow is modified');
		});

		await feature.it('updates validators when a workflow is updated (FS_WORKFLOW_003, FS_WORKFLOW_021)', () => {
			validator_2.workflowId = workflow_4.id;
			workflow_4.id = 'IMPORTANT_FLAG';
			assert.equal(validator_2.workflowId, 'IMPORTANT_FLAG', 'Property workflowId is now set to "IMPORTANT_FLAG" for validator "MUST_BE_AFTER_STUDY_START_DATE"');
			workflow_4.id = 'PROTOCOL_DEVIATION';
			assert.equal(validator_2.workflowId, 'PROTOCOL_DEVIATION', 'Property workflowId is now set to "IMPORTANT_FLAG" for validator "MUST_BE_AFTER_STUDY_START_DATE"');
		});

		//delete workflow
		await feature.it('deletes workflow properly and updates profiles', () => {
			assert.equal(study.workflows.length, 4, 'Study contains 4 workflows');
			assert.equal(study.workflowWidgets.length, 1, 'Study contains 1 workflow widget');
			workflow_2['delete']();
			assert.equal(study.workflows.length, 3, 'Study contains 3 workflows');
			assert.notOk(study.workflows.includes(workflow_2), 'Study no more contains workflow');
			assert.notOk(profile.grantedWorkflowIds.hasOwnProperty('DATA_VERIFICATION'), 'Profile no more contains id of workflow "DATA_VERIFICATION"');
		});

		await feature.it('deletes workflow properly and updates validators', () => {
			workflow_4['delete']();
			assert.equal(validator_2.workflowId, undefined, 'Validator is no more linked with workflow with id "PROTOCOL_DEVIATION"');
		});
	});

	assert.equal(global_listener.deletions.length, 5, '5 deletions have occurred (1 feature, 1 profile, 2 workflows and 1 workflow widget');

	await bundle.describe('Action', async feature => {
		//change action id
		await feature.it('updates profiles and workflow states when an action is updated', () => {
			action.id = 'SAVE';
			assert.notOk(profile.grantedWorkflowIds[action.workflow.id].childRights.hasOwnProperty('REVIEW'), 'Profile no more contains id of action "REVIEW"');
			assert.ok(profile.grantedWorkflowIds[action.workflow.id].childRights.hasOwnProperty('SAVE'), 'Profile contains id of action "SAVE"');
			assert.notOk(state.possibleActionIds.includes('REVIEW'), 'State no more contains action with id "REVIEW"');
			assert.ok(state.possibleActionIds.includes('SAVE'), 'State contains action with id "SAVE"');
		});

		//delete action
		await feature.it('deletes action properly and updates workflows and workflow states', () => {
			action['delete']();
			assert.notOk(profile.grantedWorkflowIds[action.workflow.id].childRights.hasOwnProperty('SAVE'), 'Profile no more contains id of action "SAVE"');
			assert.notOk(state.possibleActionIds.includes('SAVE'), 'State no more contains action with id "SAVE"');
			assert.notOk(workflow.actions.includes(action), 'Workflow no more contains action');
		});
	});

	await bundle.describe('Language', async feature => {
		//modify and delete language and check modifications on scope model and languages
		await feature.it('updates other languages and scope models when a language is updated', () => {
			scope_model.shortname.de = 'Der Patient';
			language_3.id = 'deu';
			assert.equal(scope_model.shortname.de, undefined, 'Scope model shortname is no longer defined for "de" as language id has been changed');
			assert.equal(scope_model.shortname.deu, 'Der Patient', 'Scope model shortname is now defined for "deu"');

			assert.equal(language_1.shortname.de, undefined, 'Language shortname is no longer defined for "de" as language id has been changed');
			assert.equal(language_1.shortname.deu, 'Englisch', 'Language shortname is now defined for "deu"');

			assert.equal(Object.keys(scope_model.shortname).length, 3, 'Scope model shortname is defined in 3 languages');
			assert.similar(Object.keys(scope_model.shortname), ['en', 'fr', 'deu'], 'Scope model shortname object contains 3 keys which are "en", "fr" and "deu"');
			language_3['delete']();
			assert.equal(Object.keys(scope_model.shortname).length, 2, 'Scope model shortname is defined in 2 languages after "de" has been deleted');
			assert.similar(Object.keys(scope_model.shortname), ['en', 'fr'], 'Scope model shortname object contains 2 keys which are "en" and "fr"');
			assert.equal(study.languages.length, 2, 'There is 2 languages in study after deleting 1 language');
		});
	});

	await bundle.describe('ScopeModel', async feature => {
		//change scope model id
		await feature.it('updates profiles and other scope models when a scope model is updated (FS_SCOPE_MODEL_003, FS_SCOPE_MODEL_004, FS_SCOPE_MODEL_005)', () => {
			scope_model_3.id = 'GROUP';
			assert.notOk(profile.isAssignedRight(Entities.ScopeModel, 'SUBSTUDY', 'WRITE'), 'Profile no more contains id of scope model "SUBSTUDY"');
			assert.ok(profile.isAssignedRight(Entities.ScopeModel, 'GROUP', 'WRITE'), 'Profile contains id of scope model "GROUP"');
			assert.notOk(scope_model.parentIds.includes('SUBSTUDY'), 'Scope model with id "PATIENT" has scope model with id "SUBSTUDY" as a parent');
			assert.ok(scope_model.parentIds.includes('GROUP'), 'Scope model with id "PATIENT" has scope model with id "GROUP" as a parent');
		});

		//delete scope model
		await feature.it('deletes scope model properly and updates profiles and other scope models (FS_SCOPE_MODEL_006, FS_SCOPE_MODEL_007, FS_SCOPE_MODEL_008)', () => {
			scope_model_3['delete']();
			assert.notOk(study.scopeModels.includes(scope_model_3), 'Study no more contains scope model with id "GROUP"');
			assert.notOk(profile.isAssignedRight(Entities.ScopeModel, 'GROUP', 'WRITE'), 'Profile no more contains id of scope model "GROUP"');
			assert.notOk(scope_model.parentIds.includes('GROUP'), 'Scope model with id "PATIENT" no more contains scope model with id "GROUP" as a parent');
		});
	});

	await bundle.describe('EventGroup', async feature => {
		//change event group id
		await feature.it('updates event models when an event group is updated', () => {
			event_model.eventGroupId = event_group.id;
			event_group.id = 'CONCOMITANT_TREATMENTS';
			assert.equal(event_model.eventGroupId, 'CONCOMITANT_TREATMENTS', 'Property eventGroup is now set to "CONCOMITANT_TREATMENTS" for event "VISIT"');
		});

		//delete event group
		await feature.it('deletes event group properly', () => {
			event_group['delete']();
			assert.equal(event_model.eventGroupId, undefined, 'Event group is no more linked with event group label with id "CONCOMITANT_TREATMENTS"');
			assert.notOk(scope_model.eventGroups.includes(event_group), 'Scope model no more contains event group label with id "CONCOMITANT_TREATMENTS"');
		});
	});

	await bundle.describe('EventModel', async feature => {
		//change event model id
		await feature.it('updates profiles when an event model is updated (FS_EVENT_MODEL_002, FS_EVENT_MODEL_003)', () => {
			event_model.id = 'FIRST_VISIT';
			assert.notOk(profile.isAssignedRight(Entities.EventModel, 'BASELINE_VISIT', 'WRITE'), 'Profile no more contains id of event model "BASELINE_VISIT"');
			assert.ok(profile.isAssignedRight(Entities.EventModel, 'FIRST_VISIT', 'WRITE'), 'Profile contains id of event model "FIRST_VISIT"');
		});

		//delete event model
		await feature.it('deletes event model properly and updates profiles (FS_EVENT_MODEL_005, FS_EVENT_MODEL_006)', () => {
			event_model_2['delete']();
			assert.notOk(scope_model.eventModels.includes(event_model_2), 'Scope model no more contains event model that has been deleted');
			assert.notOk(profile.isAssignedRight(Entities.EventModel, 'SCREENING', 'WRITE'), 'Profile no more contains id of event model "SCREENING"');
		});
	});

	await bundle.describe('FieldModel', async feature => {
		//change field model id
		await feature.it('updates timeline graphs when an field model is updated', () => {
			assert.equal(timeline_graph_section.valueFieldModelId, 'EDSS', 'Graph section value field model is field model with id "EDSS"');
			field_model_4.id = 'DISABILITY_SCORE';
			assert.equal(timeline_graph_section.valueFieldModelId, 'DISABILITY_SCORE', 'Graph section value field model is field model with id "DISABILITY_SCORE"');
		});

		//delete field model
		await feature.it('deletes field model properly and updates timeline graphs', () => {
			assert.equal(timeline_graph_section.valueFieldModelId, 'DISABILITY_SCORE', 'Graph config section value field model is field model with id "DISABILITY_SCORE"');
			field_model_4['delete']();
			assert.equal(timeline_graph_section.valueFieldModelId, undefined, 'Graph config section value field model has been cleared');
		});
	});

	//modify graph section
	timeline_graph_section.valueFieldModelId = 'AMBULATION';

	await bundle.describe('DatasetModel', async feature => {
		await feature.it('updates profiles, event models, timeline graphs and layouts when a dataset model is updated (FS_DATASET_MODEL_003, FS_DATASET_MODEL_005)', () => {
			//change dataset model id
			dataset_model_2.id = 'VISIT_DOC';
			assert.notOk(profile.isAssignedRight(Entities.DatasetModel, 'VISIT_DOCUMENT', 'WRITE'), 'Profile no more contains id of dataset model "VISIT_DOCUMENT"');
			assert.ok(profile.isAssignedRight(Entities.DatasetModel, 'VISIT_DOC', 'WRITE'), 'Profile contains id of dataset model "VISIT_DOC"');
			assert.ok(event_model.datasetModelIds.includes('VISIT_DOC'), 'Event model contains dataset model with id "VISIT_DOC"');
			assert.equal(timeline_graph_section.datasetModelId, 'VISIT_DOC', 'Graph section dataset model id has been changed to "VISIT_DOC"');
			//restore dataset model id
			dataset_model_2.id = 'VISIT_DOCUMENT';
			assert.notOk(profile.isAssignedRight(Entities.DatasetModel, 'VISIT_DOC', 'WRITE'), 'Profile no more contains id of dataset model "VISIT_DOC"');
			assert.ok(profile.isAssignedRight(Entities.DatasetModel, 'VISIT_DOCUMENT', 'WRITE'), 'Profile contains id of dataset model "VISIT_DOCUMENT"');
			assert.ok(event_model.datasetModelIds.includes('VISIT_DOCUMENT'), 'Event model contains dataset model with id "VISIT_DOCUMENT"');
			assert.equal(timeline_graph_section.datasetModelId, 'VISIT_DOCUMENT', 'Graph section dataset model id has been changed to "VISIT_DOCUMENT"');

			assert.equal(layout_3.datasetModelId, 'TREATMENT', 'Layout refers dataset model with id "TREATMENT"');
			dataset_model_3.id = 'DRUG';
			assert.equal(layout_3.datasetModelId, 'DRUG', 'Layout now refers dataset model with id "DRUG"');
		});

		//delete dataset model
		await feature.it('deletes dataset model properly and updates profiles, event models, timeline graphs and layouts (FS_DATASET_MODEL_007, FS_DATASET_MODEL_008, FS_DATASET_MODEL_010)', () => {
			dataset_model_2['delete']();
			assert.notOk(study.datasetModels.includes(dataset_model_2), 'Study no more contains dataset model with id "VISIT_DOCUMENT"');
			assert.notOk(profile.isAssignedRight(Entities.DatasetModel, 'VISIT_DOCUMENT'), 'Profile no more contains id of dataset model "VISIT_DOCUMENT"');
			assert.notOk(event_model.datasetModelIds.includes('VISIT_DOCUMENT'), 'Event model contains dataset model with id "VISIT_DOCUMENT"');

			assert.equal(timeline_graph_section.datasetModelId, undefined, 'Graph section no more refers dataset model with id "VISIT_DOCUMENT"');
			assert.equal(timeline_graph_section.valueFieldModelId, undefined, 'Graph section value field model list has been cleared');

			assert.equal(form_model.layouts.length, 3, 'Form model has 3 layouts');
			dataset_model_3['delete']();
			assert.equal(form_model.layouts.length, 2, 'Form model has only 2 layouts after a dataset model referred by one of the layout of the form model has been deleted');
		});
	});

	await bundle.describe('Validator', async feature => {
		//change validator id
		await feature.it('updates field models when a validator is updated (FS_VALIDATOR_003)', () => {
			validator.id = 'MUST_BE_MALE';
			assert.notOk(field_model_1.validatorIds.includes('REQUIRED'), 'Field model no more contains validator with id "REQUIRED"');
			assert.ok(field_model_1.validatorIds.includes('MUST_BE_MALE'), 'Field model contains validator with id "MUST_BE_MALE"');
		});

		//delete validator
		await feature.it('deletes validator properly and updates field models (FS_VALIDATOR_004, FS_VALIDATOR_005)', () => {
			validator['delete']();
			assert.notOk(study.validators.includes(validator), 'Study no more contains validator');
			assert.notOk(field_model_1.validatorIds.includes('MUST_BE_MALE'), 'Field model no more contains validator with id "MUST_BE_MALE"');
		});
	});

	await bundle.describe('FieldModel', async feature => {
		//delete field model
		await feature.it('deletes field model properly and updates cells', () => {
			assert.ok(form_model.getFieldModels().includes(field_model_1), 'Form model contains first field model');
			field_model_1['delete']();
			assert.equal(cell_1.fieldModelId, undefined, 'Cell has been cleaned');
			assert.notOk(form_model.getFieldModels().includes(field_model_1), 'Form model no more contains first field model');

			assert.ok(dataset_model.fieldModels.includes(field_model_3), 'Dataset model contains field model which has been added to dataset model');
			field_model_3['delete']();
			assert.notOk(dataset_model.fieldModels.includes(field_model_3), 'Dataset model does not contains field model which has been deleted');
		});
	});

	await bundle.describe('FormModel', async feature => {
		//change form model id
		await feature.it('updates event models and profiles when a form model is updated', () => {
			form_model_2.id = 'DISABILITY_SCALE';
			assert.notOk(event_model.formModelIds.includes('EDSS'), 'Event no more contains form model with id "EDSS"');
			assert.ok(event_model.formModelIds.includes('DISABILITY_SCALE'), 'Event contains form model with id "DISABILITY_SCALE"');
			assert.ok(profile.isAssignedRight(Entities.FormModel, 'DISABILITY_SCALE', 'WRITE'), 'Profile contains id of form model "DISABILITY_SCALE"');
		});

		//delete form model
		await feature.it('deletes form model properly and updates event models and profiles', () => {
			form_model_2['delete']();
			assert.notOk(event_model.formModelIds.includes('DISABILITY_SCALE'), 'Event no more contains form model with id "DISABILITY_SCALE"');
			assert.notOk(study.formModels.includes(form_model_2), 'Study no more contains form model');
			assert.notOk(profile.isAssignedRight(Entities.FormModel, 'DISABILITY_SCALE', 'WRITE'), 'Profile no more contains id of form model "DISABILITY_SCALE"');
		});
	});

	await bundle.describe('Layout', async feature => {
		//delete layout
		await feature.it('deletes layout properly and updates cells', () => {
			layout_2['delete']();
			assert.ok(cell_2.visibilityCriteria.isEmpty(), 'Cell visibility criteria is empty');
		});
	});

	await bundle.describe('PaymentStep', async feature => {
		//delete payment step
		await feature.it('deletes payment step properly', () => {
			assert.equal(payment_plan.steps.length, 1, 'Payment plan contains 1 step');
			payment_step['delete']();
			assert.notOk(payment_plan.steps.includes(payment_step), 'Payment plan no more contains payment step');
			assert.equal(payment_plan.steps.length, 0, 'Payment plan contains 0 step');
		});
	});

	await bundle.describe('PaymentPlan', async feature => {
		//delete payment plan
		await feature.it('deletes payment plan properly', () => {
			payment_plan['delete']();
			assert.notOk(study.paymentPlans.includes(payment_plan), 'Study no more contains payment plan');
		});
	});

	await bundle.describe('Menu', async feature => {
		await feature.it('updates profiles when a menu is updated (FS_MENU_003)', () => {
			//change menu id
			menu_1.id = 'SUPPORT';
			assert.notOk(profile.isAssigned(Entities.Menu, 'HELP'), 'Profile no more contains id of menu "CONTACT"');
			assert.ok(profile.isAssigned(Entities.Menu, 'SUPPORT'), 'Profile contains id of menu "SYSTEM_REQUIREMENTS"');

			//change submenu id
			submenu_1.id = 'SYSTEM_REQUIREMENTS';
			assert.notOk(profile.isAssigned(Entities.Menu, 'CONTACT'), 'Profile no more contains id of menu "CONTACT"');
			assert.ok(profile.isAssigned(Entities.Menu, 'SYSTEM_REQUIREMENTS'), 'Profile contains id of menu "SYSTEM_REQUIREMENTS"');
		});

		//delete menus
		await feature.it('deletes menu properly and updates profiles (FS_MENU_004, FS_MENU_005)', () => {
			assert.equal(study.menus.length, 3, 'There is 3 menus in study');
			menu['delete']();
			assert.equal(study.menus.length, 2, 'There is 2 menus in study after a menu has been deleted');
			menu_1['delete']();
			assert.equal(study.menus.length, 1, 'There is 1 menu in study after a menu has been deleted');
			assert.notOk(profile.isAssigned(Entities.Menu, 'SUPPORT'), 'Profile no more contains id of menu "SUPPORT"');
			assert.notOk(profile.isAssigned(Entities.Menu, 'SYSTEM_REQUIREMENTS'), 'Profile no more contains id of menu "SYSTEM_REQUIREMENTS" which was a submenu id of a deleted menu');
		});
	});

	await bundle.describe('ResourceCategory', async feature => {
		//delete resource category
		await feature.it('deletes resource category properly', () => {
			resource_category['delete']();
			assert.notOk(study.resourceCategories.includes(resource_category), 'Study no more contains resource category with id "SAMPLE"');
		});
	});

	await bundle.describe('PrivacyPolicy', async feature => {
		//delete privacy policy
		await feature.it('deletes privacy policy properly', () => {
			privacy_policy['delete']();
			assert.notOk(study.privacyPolicies.includes(privacy_policy), 'Study no more contains privacy policy with id "EULA"');
		});
	});

	bundle.end();
}
