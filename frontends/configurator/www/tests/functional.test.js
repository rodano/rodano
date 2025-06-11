import '../basic-tools/extension.js';

import {Study} from '../model/config/entities/study.js';
import {ConfigHelpers} from '../model_config.js';
import {StudyLoader} from '../study_loader.js';
import {EntitiesForms} from '../entities_forms.js';
import {EntitiesPlaceholders} from '../entities_placeholders.js';

export default async function test(bundle, assert, driver) {
	bundle.begin();

	//reset url
	window.location.hash = '';

	//reset clipboard
	navigator.clipboard.writeText('');

	//load all forms and placeholders to make testing easier
	await EntitiesForms.LoadAll();
	await EntitiesPlaceholders.LoadAll();

	//create and customize a study
	const study = new Study();
	ConfigHelpers.InsertRevivedStaticNodes(study);
	study.id = 'TEST';
	study.shortname = {en: 'Test'};
	study.defaultLanguageId = 'en';

	//update ui
	StudyLoader.Load(study);
	/**@type {HTMLDialogElement}*/ (document.getElementById('welcome')).close();
	/**@type {HTMLDialogElement}*/ (document.getElementById('authentication')).close();

	//edit study
	await driver.click('a[title="TEST"]');
	await driver.wait();
	await driver.doubleClick(await driver.getShadow('#study_language_ids', 'div > ul > li:nth-child(1)'));
	await driver.doubleClick(await driver.getShadow('#study_language_ids', 'div > ul > li:nth-child(1)'));
	await driver.submit('#edit_study_form');

	await driver.click('a[title="TEST"]');
	await driver.wait();
	await driver.type(await driver.getShadow('#study_shortname', 'input'), 'Test Study');
	await driver.type(await driver.getShadow('#study_longname', 'input'), 'Test Study');
	await driver.submit('#edit_study_form');

	await bundle.describe('creation', async feature => {
		//add language
		await feature.it('adds a language (FS_LANGUAGE_001)', async () => {
			await driver.click('#tree img[title="Add language"]');
			await driver.wait();
			await driver.type('#language_id', 'de');
			await driver.type(await driver.getShadow('#language_shortname', 'select'), 'en');
			await driver.type(await driver.getShadow('#language_shortname', 'input'), 'German');
			await driver.submit('#edit_language_form');

			assert.equal(study.languages.length, 3, 'Language is added');
			assert.equal(study.languages.last().id, 'de', 'Language is added with good id');
			assert.equal(study.languages.last().shortname['en'], 'German', 'Language is added with good shortname');
		});

		const features_number = study.features.length;

		//add feature
		await feature.it('adds a feature (FS_FEATURE_001)', async () => {
			await driver.click('#tree img[title="Add feature"]');
			await driver.wait();
			await driver.type('#feature_id', 'TEST_FEATURE');
			await driver.type(await driver.getShadow('#feature_shortname', 'input'), 'Test feature');
			await driver.submit('#edit_feature_form');

			assert.equal(study.features.length, features_number + 1, 'Feature is added');
			assert.equal(study.features.last().id, 'TEST_FEATURE', 'Feature is added with good id');
			assert.equal(study.features.last().shortname['en'], 'Test feature', 'Feature is added with good shortname');
		});

		await feature.it('adds a profile (FS_PROFILE_001)', async () => {
			//add profile INVESTIGATOR
			await driver.click('#tree img[title="Add profile"]');
			await driver.wait();
			await driver.type('#profile_id', 'INVESTIGATOR');
			await driver.type(await driver.getShadow('#profile_shortname', 'input'), 'Investigator');
			await driver.submit('#edit_profile_form');

			assert.equal(study.profiles.length, 1, 'Profile is added');
			assert.equal(study.profiles.last().id, 'INVESTIGATOR', 'Profile is added with good id');
			assert.equal(study.profiles.last().shortname['en'], 'Investigator', 'Profile is added with shortname');

			//add profile NURSE
			await driver.click('#tree img[title="Add profile"]');
			await driver.type('#profile_id', 'NURSE');
			await driver.type(await driver.getShadow('#profile_shortname', 'input'), 'Nurse');
			await driver.submit('#edit_profile_form');

			assert.equal(study.profiles.length, 2, 'Profile is added');
			assert.equal(study.profiles.last().id, 'NURSE', 'Profile is added with good id');
			assert.equal(study.profiles.last().shortname['en'], 'Nurse', 'Profile is added with good shortname');
		});

		await feature.it('adds a scope model (FS_SCOPE_MODEL_001)', async () => {
			//add scope model STUDY
			await driver.click('#tree img[title="Add scope model"]');
			await driver.wait();
			await driver.type('#scope_model_id', 'STUDY');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Study');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'select'), 'fr');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Etude');
			await driver.type('#scope_model_expected_number', 1);
			await driver.type('#scope_model_max_number', 1);
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.scopeModels.length, 1, 'Scope model is added');
			assert.equal(study.scopeModels.last().id, 'STUDY', 'Scope model is added with good id');
			assert.equal(study.scopeModels.last().shortname['en'], 'Study', 'Scope model is added with good shortname');

			//add scope model COUNTRY
			await driver.click('#tree img[title="Add scope model"]');
			await driver.wait();
			await driver.type('#scope_model_id', 'COUNTRY');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Country');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'select'), 'fr');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Pays');
			await driver.type('#scope_model_expected_number', 5);
			await driver.type('#scope_model_max_number', 5);
			await driver.doubleClick(await driver.getShadow('#scope_model_parent_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.scopeModels.length, 2, 'Scope model is added');
			assert.equal(study.scopeModels.last().id, 'COUNTRY', 'Scope model is added with good id');
			assert.equal(study.scopeModels.last().shortname['en'], 'Country', 'Scope model is added with good shortname');
			assert.equal(study.getScopeModel('COUNTRY').parentIds[0], 'STUDY', 'Scope model "COUNTRY" has "STUDY" as a parent');

			//add scope model CENTER
			await driver.click('#tree img[title="Add scope model"]');
			await driver.wait();
			await driver.type('#scope_model_id', 'CENTER');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Center');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'select'), 'fr');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Centre');
			await driver.doubleClick(await driver.getShadow('#scope_model_parent_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.scopeModels.length, 3, 'Scope model is added');
			assert.equal(study.scopeModels.last().id, 'CENTER', 'Scope model is added with good id');
			assert.equal(study.scopeModels.last().shortname['en'], 'Center', 'Scope model is added with good shortname');
			assert.equal(study.getScopeModel('CENTER').parentIds[0], 'COUNTRY', 'Scope model "CENTER" has "COUNTRY" as a parent');

			//add scope model PATIENT
			await driver.click('#tree img[title="Add scope model"]');
			await driver.wait();
			await driver.type('#scope_model_id', 'PATIENT');
			await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Patient');
			await driver.doubleClick(await driver.getShadow('#scope_model_parent_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.scopeModels.length, 4, 'Scope model is added');
			assert.equal(study.scopeModels.last().id, 'PATIENT', 'Scope model is added with good id');
			assert.equal(study.scopeModels.last().shortname['en'], 'Patient', 'Scope model is added with good shortname');
			assert.equal(study.getScopeModel('PATIENT').parentIds[0], 'CENTER', 'Scope model "PATIENT" has "CENTER" as a parent');
		});

		//add event group UNSCHEDULED
		/*await feature.it('adds a event group (FS_EVENT_GROUP_001)', async () => {
			await driver.click('#tree img[title="Add event group"]');
			await driver.wait();
			await driver.type('#event_group_id', 'UNSCHEDULED');
			await driver.type(await driver.getShadow('#event_group_shortname', 'input'), 'Unscheduled');
			await driver.type('#event_group_scope_model_id', 'PATIENT');
			await driver.submit('#edit_event_group_general');

			assert.equal(study.eventGroups.length, 1, 'Event group is added');
			assert.equal(study.eventGroups.last().id, 'UNSCHEDULED', 'Event group is added with good id');
			assert.equal(study.eventGroups.last().shortname['en'], 'Unscheduled', 'Event group is added with good shortname');
			assert.equal(study.getScopeModel('PATIENT').getEventGroups()[0].id, 'UNSCHEDULED', 'Scope model "PATIENT" has a new event group called "SCHEDULING"');

			//add event group SCHEDULING
			await driver.click('#tree img[title="Add event group"]');
			await driver.wait();
			await driver.type('#event_group_id', 'SCHEDULING');
			await driver.type(await driver.getShadow('#event_group_shortname', 'input'), 'Scheduling');
			await driver.submit('#edit_event_group_general');

			assert.equal(study.eventGroups.length, 2, 'Event group is added');
			assert.equal(study.eventGroups.last().id, 'SCHEDULING', 'Event group is added with good id');
			assert.equal(study.eventGroups.last().shortname['en'], 'Scheduling', 'Event group is added with good shortname');
		});*/

		await feature.it('adds an event model (FS_EVENT_MODEL_001)', async () => {
			//add event model UNEXPECTED
			await driver.click('#tree ul.scope_model > li:has(a[href="#node=Study:TEST|ScopeModel:PATIENT"]) img[title="Add event model"]');
			await driver.wait();
			await driver.type('#event_model_id', 'UNEXPECTED');
			await driver.type(await driver.getShadow('#event_model_shortname', 'input'), 'Unexpected');
			await driver.submit('#edit_event_model_form');

			const scope_model = study.getScopeModel('PATIENT');
			assert.equal(scope_model.eventModels.length, 1, 'Event model is added');
			assert.equal(scope_model.eventModels.last().id, 'UNEXPECTED', 'Event model is added with good id');
			assert.equal(scope_model.eventModels.last().shortname['en'], 'Unexpected', 'Event model is added with good shortname');

			//add event model EXPECTED
			await driver.click('#tree ul.scope_model > li:has(a[href="#node=Study:TEST|ScopeModel:PATIENT"]) img[title="Add event model"]');
			await driver.wait();
			await driver.type('#event_model_id', 'EXPECTED');
			await driver.type(await driver.getShadow('#event_model_shortname', 'input'), 'Expected');
			await driver.submit('#edit_event_model_form');

			assert.equal(scope_model.eventModels.length, 2, 'Event model is added');
			assert.equal(scope_model.eventModels.last().id, 'EXPECTED', 'Event model is added with good id');
			assert.equal(scope_model.eventModels.last().shortname['en'], 'Expected', 'Event model is added with good shortname');
		});

		//add dataset model PATIENT_DOCUMENT
		await feature.it('adds a dataset model (FS_DATASET_MODEL_001)', async () => {
			await driver.click('#tree img[title="Add dataset model"]');
			await driver.wait();
			await driver.type('#dataset_model_id', 'PATIENT_DOCUMENTATION');
			await driver.type(await driver.getShadow('#dataset_model_shortname', 'input'), 'Patient documentation');

			await driver.submit('#edit_dataset_model_form');
			assert.equal(study.datasetModels.length, 1, 'Dataset model is added');
			assert.equal(study.datasetModels.last().id, 'PATIENT_DOCUMENTATION', 'Dataset model is added with good id');
			assert.equal(study.datasetModels.last().shortname['en'], 'Patient documentation', 'Dataset model is added with good shortname');
			assert.ok(study.getDatasetModel('PATIENT_DOCUMENTATION').fieldModels.isEmpty(), 'Dataset model has no field model');
		});

		//add field model DATE_OF_BIRTH
		await feature.it('adds an field model (FS_FIELD_MODEL_001)', async () => {
			await driver.click(await driver.eval('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION"]', e => e.nextElementSibling));
			await driver.wait();
			await driver.type('#field_model_id', 'DATE_OF_BIRTH');
			await driver.type('#field_model_type', 'DATE');
			await driver.type(await driver.getShadow('#field_model_shortname', 'input'), 'Date of birth');
			await driver.submit('#edit_field_model_form');

			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION').fieldModels.length, 1, 'Field model is created');
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION').fieldModels.last().id, 'DATE_OF_BIRTH', 'Field model is create with good id');
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION').fieldModels.last().shortname['en'], 'Date of birth', 'Field model is create with good shortname');
		});

		//add dataset model ADDRESS
		await feature.it('adds another dataset model (FS_DATASET_MODEL_001)', async () => {
			await driver.click('#tree img[title="Add dataset model"]');
			await driver.wait();
			await driver.type('#dataset_model_id', 'ADDRESS');
			await driver.type(await driver.getShadow('#dataset_model_shortname', 'input'), 'Address');
			await driver.submit('#edit_dataset_model_form');

			assert.equal(study.datasetModels.length, 2, 'Dataset model is added');
			assert.equal(study.datasetModels.last().id, 'ADDRESS', 'Dataset model is added with good id');
			assert.equal(study.datasetModels.last().shortname['en'], 'Address', 'Dataset model is added with good shortname');
			assert.ok(study.getDatasetModel('ADDRESS').fieldModels.isEmpty(), 'Dataset model has no field model');
		});

		//add field model STREET
		await feature.it('adds another field model (FS_FIELD_MODEL_001)', async () => {
			await driver.click(await driver.eval('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS"]', e => e.nextElementSibling));
			await driver.wait();
			await driver.type('#field_model_id', 'STREET');
			await driver.type('#field_model_type', 'STRING');
			await driver.type(await driver.getShadow('#field_model_shortname', 'input'), 'Street');
			await driver.submit('#edit_field_model_form');

			assert.equal(study.getDatasetModel('ADDRESS').fieldModels.length, 1, 'Field model is created');
			assert.equal(study.getDatasetModel('ADDRESS').fieldModels.last().id, 'STREET', 'Field model is create with good id');
			assert.equal(study.getDatasetModel('ADDRESS').fieldModels.last().shortname['en'], 'Street', 'Field model is create with good shortname');
		});

		//add validator VALIDATOR_1
		await feature.it('adds a validator (FS_VALIDATOR_001)', async () => {
			await driver.click('#tree img[title="Add validator"]');
			await driver.wait();
			await driver.type('#validator_id', 'VALIDATOR_1');
			await driver.type(await driver.getShadow('#validator_shortname', 'input'), 'Validator 1');
			await driver.type(await driver.getShadow('#validator_message', 'input'), 'Value is wrong');
			await driver.submit('#edit_validator_form');

			assert.equal(study.validators.length, 1, 'Validator is created');
			assert.equal(study.validators.last().id, 'VALIDATOR_1', 'Validator is created with good id');
			assert.equal(study.validators.last().shortname['en'], 'Validator 1', 'Validator is created with good shortname');
		});

		//add workflow WORKFLOW
		await feature.it('adds a workflow (FS_WORKFLOW_001)', async () => {
			await driver.click('img[title="Add workflow"]');
			await driver.wait();
			await driver.type('#workflow_id', 'WORKFLOW');
			await driver.type(await driver.getShadow('#workflow_shortname', 'input'), 'Workflow');
			await driver.submit('#edit_workflow_form');

			assert.equal(study.workflows.length, 1, 'Workflow is created');
			assert.equal(study.workflows.last().id, 'WORKFLOW', 'Workflow is created with good id');
			assert.equal(study.workflows.last().shortname['en'], 'Workflow', 'Workflow is created with good shortname');
		});

		//add action
		await feature.it('adds a action (FS_ACTION_001)', async () => {
			await driver.click(await driver.eval('#tree ul.workflow > li a[href="#node=Study:TEST|Workflow:WORKFLOW"]', e => e.previousElementSibling));
			await driver.click('#tree img[title="Add action"]');
			await driver.wait();
			await driver.type('#action_id', 'ACTION');
			await driver.type(await driver.getShadow('#action_shortname', 'input'), 'Action');
			await driver.submit('#edit_action_form');

			assert.equal(study.getWorkflow('WORKFLOW').actions.length, 1, 'Action is created');
			assert.equal(study.getWorkflow('WORKFLOW').actions.last().id, 'ACTION', 'Action is created with good id');
			assert.equal(study.getWorkflow('WORKFLOW').actions.last().shortname['en'], 'Action', 'Action is created with good shortname');
		});

		//add state
		await feature.it('adds a state (FS_WORKFLOW_STATE_001)', async () => {
			await driver.click('#tree img[title="Add state"]');
			await driver.wait();
			await driver.type('#workflow_state_id', 'STATE');
			await driver.type(await driver.getShadow('#workflow_state_shortname', 'input'), 'State');
			await driver.submit('#edit_workflow_state_form');

			assert.equal(study.getWorkflow('WORKFLOW').states.length, 1, 'State is created');
			assert.equal(study.getWorkflow('WORKFLOW').states.last().id, 'STATE', 'State is created with good id');
			assert.equal(study.getWorkflow('WORKFLOW').states.last().shortname['en'], 'State', 'State is created with good shortname');
		});

		await feature.it('adds a form model (FS_FORM_MODEL_001)', async () => {
			//add form model DEMOGRAPHICS
			await driver.click('#tree img[title="Add form model"]');
			await driver.wait();
			await driver.type('#form_model_id', 'DEMOGRAPHICS');
			await driver.type(await driver.getShadow('#form_model_shortname', 'input'), 'Demographics');
			await driver.submit('#edit_form_model_form');

			assert.equal(study.formModels.length, 1, 'Form model is created');
			assert.equal(study.formModels.last().id, 'DEMOGRAPHICS', 'Form model is created with good id');
			assert.equal(study.formModels.last().shortname['en'], 'Demographics', 'Form model is created with good shortname');

			//add form model LOCATION
			await driver.click('#tree img[title="Add form model"]');
			await driver.wait();
			await driver.type('#form_model_id', 'LOCATION');
			await driver.type(await driver.getShadow('#form_model_shortname', 'input'), 'Location');
			await driver.submit('#edit_form_model_form');

			assert.equal(study.formModels.length, 2, 'Form model is created');
			assert.equal(study.formModels.last().id, 'LOCATION', 'Form model is created with good id');
			assert.equal(study.formModels.last().shortname['en'], 'Location', 'Form model is created with good shortname');
		});

		await feature.it('adds a menu (FS_MENU_001)', async () => {
			//add menu
			await driver.click('#tree img[title="Add menu"]');
			await driver.wait();
			await driver.type('#menu_id', 'HOME');
			await driver.type(await driver.getShadow('#menu_shortname', 'input'), 'Home');
			await driver.submit('#edit_menu_form');

			assert.equal(study.menus.length, 1, 'Menu is added');
			assert.equal(study.menus.last().id, 'HOME', 'Menu is added with good id');
			assert.equal(study.menus.last().shortname['en'], 'Home', 'Menu is added with good shortname');

			//add submenu
			await driver.click(await driver.eval('#tree ul.menu > li a[href="#node=Study:TEST|Menu:HOME"]', e => e.parentNode.children[2]));
			await driver.wait();
			await driver.type('#menu_id', 'DASHBOARD');
			await driver.type(await driver.getShadow('#menu_shortname', 'input'), 'Dashboard');
			await driver.submit('#edit_menu_form');

			assert.equal(study.getMenu('HOME').submenus.length, 1, 'Submenu is added');
			assert.equal(study.getMenu('HOME').submenus[0].id, 'DASHBOARD', 'Submenu is added with good id');
			assert.equal(study.getMenu('HOME').submenus[0].shortname['en'], 'Dashboard', 'Submenu is added with good shortname');
			assert.equal(study.getMenu('HOME').submenus[0].parent.id, 'HOME', 'Submenu is a child of an other menu');
		});

		const resource_categories_number = study.resourceCategories.length;

		//add resource category
		await feature.it('adds a resource category (FS_RESOURCE_CATEGORY_001)', async () => {
			await driver.click('#tree img[title="Add resource category"]');
			await driver.wait();
			await driver.type('#resource_category_id', 'RESOURCE_CATEGORY_1');
			await driver.type(await driver.getShadow('#resource_category_shortname', 'input'), 'Resource category 1');
			await driver.submit('#edit_resource_category_form');

			assert.equal(study.resourceCategories.length, resource_categories_number + 1, 'Resource category is added');
			assert.equal(study.resourceCategories.last().id, 'RESOURCE_CATEGORY_1', 'Resource category is added with good id');
			assert.equal(study.resourceCategories.last().shortname['en'], 'Resource category 1', 'Resource category is added with good shortname');
		});

		//add workflow widget
		await feature.it('adds a workflow widget (FS_WORKFLOW_WIDGET_001)', async () => {
			await driver.click('#tree img[title="Add workflow widget"]');
			await driver.wait();
			await driver.type('#workflow_widget_id', 'WORKFLOW_WIDGET_1');
			await driver.type(await driver.getShadow('#workflow_widget_shortname', 'input'), 'Workflow widget 1');
			await driver.submit('#edit_workflow_widget_form');

			assert.equal(study.workflowWidgets.length, 1, 'Workflow widget is added');
			assert.equal(study.workflowWidgets.last().id, 'WORKFLOW_WIDGET_1', 'Workflow widget is added with good id');
			assert.equal(study.workflowWidgets.last().shortname['en'], 'Workflow widget 1', 'Workflow widget is added with good shortname');
		});

		//add privacy policy
		await feature.it('adds a privacy policy (FS_PRIVACY_POLICY_001)', async () => {
			await driver.click('#tree img[title="Add privacy policy"]');
			await driver.wait();
			await driver.type('#privacy_policy_id', 'PRIVACY_POLICY_1');
			await driver.type(await driver.getShadow('#privacy_policy_shortname', 'input'), 'Privacy policy 1');
			await driver.submit('#edit_privacy_policy_form');

			assert.equal(study.privacyPolicies.length, 1, 'Privacy policy is added');
			assert.equal(study.privacyPolicies.last().id, 'PRIVACY_POLICY_1', 'Privacy policy is added with good id');
			assert.equal(study.privacyPolicies.last().shortname['en'], 'Privacy policy 1', 'Privacy policy is added with good shortname');
		});
	});

	await bundle.describe('rights', async feature => {
		//give right on profile to a profile
		await feature.it('gives right on a profile', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=Profile"]');
			await driver.wait();
			await driver.click('span[title="Give right WRITE on NURSE for profile INVESTIGATOR"]');

			assert.ok(study.profiles[0].grantedProfileIdRights.hasOwnProperty('NURSE'), 'Profile has right on other profile');
		});

		//give right on feature to a profile
		await feature.it('gives right on a feature', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=Feature"]');
			await driver.wait();
			await driver.click('img[title="Give right on TEST_FEATURE for profile INVESTIGATOR"]');

			assert.equal(study.getProfile('INVESTIGATOR').grantedFeatureIds[0], 'TEST_FEATURE', 'Profile has right on feature');
		});

		//give right on scope models to a profile
		await feature.it('gives right on scope models', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=ScopeModel"]');
			await driver.wait();
			await driver.click(await driver.eval('#matrix a[title="STUDY"]', e => e.parentNode.nextElementSibling.children[1]));
			await driver.click(await driver.eval('#matrix a[title="COUNTRY"]', e => e.parentNode.nextElementSibling.children[1]));
			await driver.click(await driver.eval('#matrix a[title="CENTER"]', e => e.parentNode.nextElementSibling.children[1]));
			await driver.click(await driver.eval('#matrix a[title="PATIENT"]', e => e.parentNode.nextElementSibling.children[0]));

			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('STUDY'), 'Profile has right on scope model "COUNTRY"');
			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('COUNTRY'), 'Profile has right on scope model "COUNTRY"');
			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('CENTER'), 'Profile has right on scope model "COUNTRY"');
			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('PATIENT'), 'Profile has right on scope model "COUNTRY"');

			assert.equal(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights['STUDY'][0], 'READ', 'Profile "INVESTIGATOR" can read study');
			assert.equal(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights['CENTER'][0], 'READ', 'Profile "INVESTIGATOR" can read center');
			assert.equal(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights['PATIENT'][0], 'WRITE', 'Profile "INVESTIGATOR" can write patient');
			assert.equal(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights['PATIENT'][1], 'READ', 'Profile "INVESTIGATOR" can read patient');
		});

		//give right on events to a profile
		await feature.it('gives right on event models', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=EventModel"]');
			await driver.wait();
			await driver.click('span[title="Give right WRITE on UNEXPECTED for profile INVESTIGATOR"]');
			await driver.click('span[title="Give right WRITE on EXPECTED for profile INVESTIGATOR"]');

			assert.ok(study.getProfile('INVESTIGATOR').grantedEventModelIdRights.hasOwnProperty('UNEXPECTED'), 'Profile has right on event model');
			assert.ok(study.getProfile('INVESTIGATOR').grantedEventModelIdRights.hasOwnProperty('EXPECTED'), 'Profile has right on event model');
		});

		//give right on dataset model to a profile
		await feature.it('gives right on dataset models', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=DatasetModel"]');
			await driver.wait();
			await driver.click('span[title="Give right WRITE on PATIENT_DOCUMENTATION for profile INVESTIGATOR"]');

			assert.equal(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights['PATIENT_DOCUMENTATION'][0], 'WRITE', 'Profile "INVESTIGATOR" can write dataset model');
			assert.equal(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights['PATIENT_DOCUMENTATION'][1], 'READ', 'Profile "INVESTIGATOR" can read dataset model');
		});

		//give right on on workflow and action to a profile
		await feature.it('gives right on workflows', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=Workflow"]');
			await driver.wait();
			await driver.click('img[title="Give right on WORKFLOW for profile INVESTIGATOR"]');
			await driver.click('img[title="Give right on ACTION of WORKFLOW for profile INVESTIGATOR"]');

			assert.ok(study.getProfile('INVESTIGATOR').grantedWorkflowIds.hasOwnProperty('WORKFLOW'), 'Profile has right on workflow');
			assert.ok(study.getProfile('INVESTIGATOR').grantedWorkflowIds['WORKFLOW'].childRights.hasOwnProperty('ACTION'), 'Profile has right on action');
		});

		//give right on form models to a profile
		await feature.it('gives right on form models', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=FormModel"]');
			await driver.wait();
			await driver.click('span[title="Give right WRITE on DEMOGRAPHICS for profile INVESTIGATOR"]');
			await driver.click('span[title="Give right WRITE on LOCATION for profile INVESTIGATOR"]');

			assert.equal(study.getProfile('INVESTIGATOR').grantedFormModelIdRights['DEMOGRAPHICS'][0], 'WRITE', 'Profile "INVESTIGATOR" can write form model');
			assert.equal(study.getProfile('INVESTIGATOR').grantedFormModelIdRights['LOCATION'][0], 'WRITE', 'Profile "INVESTIGATOR" can write form model');
		});

		//give right on menus to a profile
		await feature.it('gives right on menus', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=Menu"]');
			await driver.wait();
			await driver.click('img[title="Give right on HOME for profile INVESTIGATOR"]');
			await driver.click('img[title="Give right on DASHBOARD for profile INVESTIGATOR"]');

			assert.equal(study.getProfile('INVESTIGATOR').grantedMenuIds[0], 'HOME', 'Profile has right on menu');
			assert.equal(study.getProfile('INVESTIGATOR').grantedMenuIds[1], 'DASHBOARD', 'Profile has right on menu');
			assert.equal(study.getProfile('INVESTIGATOR').grantedMenuIds.length, 2, 'Profile has right on menu');
		});

		//give right on a resource category to a profile
		await feature.it('gives right on resources categories', async () => {
			await driver.focus('#menubar > li:nth-child(4) > button:nth-child(1)');
			await driver.click('a[href="#matrix=profile&entity=ResourceCategory"]');
			await driver.wait();
			await driver.click('img[title="Give right on RESOURCE_CATEGORY_1 for profile INVESTIGATOR"]');

			assert.equal(study.getProfile('INVESTIGATOR').grantedCategoryIds[0], 'RESOURCE_CATEGORY_1', 'Profile has right on resource category');
		});
	});

	await bundle.describe('associations', async feature => {
		//add action to workflow
		await feature.it('links action to workflow', async () => {
			await driver.click('#tree ul.workflow > li a[href="#node=Study:TEST|Workflow:WORKFLOW"]');
			await driver.wait();
			await driver.click('#workflow_mandatory');
			await driver.type('#workflow_action_id', 'ACTION');
			await driver.submit('#edit_workflow_form');

			assert.equal(study.getWorkflow('WORKFLOW').actionId, 'ACTION', 'Workflow is linked to action');
		});

		//add action to workflow state
		await feature.it('links action to workflow state', async () => {
			await driver.click('#tree ul.workflow_state > li a[href="#node=Study:TEST|Workflow:WORKFLOW|WorkflowState:STATE"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#workflow_state_actions_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_workflow_state_form');

			assert.equal(study.getWorkflow('WORKFLOW').getState('STATE').possibleActionIds[0], 'ACTION', 'Workflow state is linked to action');
		});

		//add dataset model and form model to scope model
		await feature.it('links form model to scope model', async () => {
			await driver.click('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:CENTER"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#scope_model_dataset_model_ids', 'div > ul > li:nth-child(1)'));
			await driver.doubleClick(await driver.getShadow('#scope_model_form_model_ids', 'div > ul > li:nth-child(2)'));
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.getScopeModel('CENTER').datasetModelIds.length, 1, 'Dataset model is linked to a scope model');
			assert.equal(study.getScopeModel('CENTER').datasetModelIds[0], 'ADDRESS', 'Dataset model is linked to a scope model');
			assert.equal(study.getScopeModel('CENTER').formModelIds.length, 1, 'Form model is linked to a scope model');
			assert.equal(study.getScopeModel('CENTER').formModelIds[0], 'LOCATION', 'Form model is linked to a scope model');
		});

		//add validator to field model
		await feature.it('links validator to field model', async () => {
			await driver.click('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION|FieldModel:DATE_OF_BIRTH"]');
			await driver.wait();
			await driver.click('a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION|FieldModel:DATE_OF_BIRTH&tab=edit_field_model_validators_workflows"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#field_model_validator_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_field_model_form');

			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION').getFieldModel('DATE_OF_BIRTH').validatorIds.length, 1, 'Validator is linked to a field model');
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION').getFieldModel('DATE_OF_BIRTH').validatorIds[0], 'VALIDATOR_1', 'Validator is linked to a field model');
		});

		//add dataset model and form model to event model
		await feature.it('links dataset model and form model to event model', async () => {
			await driver.click('#tree ul.event_model > li a[href="#node=Study:TEST|ScopeModel:PATIENT|EventModel:UNEXPECTED"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#event_model_dataset_model_ids', 'div > ul > li:nth-child(2)'));
			await driver.doubleClick(await driver.getShadow('#event_model_form_model_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_event_model_form');

			const event_model = study.getScopeModel('PATIENT').getEventModel('UNEXPECTED');
			assert.equal(event_model.datasetModelIds.length, 1, 'Dataset model is added to an event model');
			assert.equal(event_model.datasetModelIds[0], 'PATIENT_DOCUMENTATION', 'Dataset model is added to an event model');
			assert.equal(event_model.formModelIds.length, 1, 'Form model is added to an event model');
			assert.equal(event_model.formModelIds[0], 'DEMOGRAPHICS', 'Form model is added to an event model');
		});

		//add workflow to scope model
		await feature.it('links workflow to scope model', async () => {
			await driver.click('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:PATIENT"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#scope_model_workflow_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_scope_model_form');

			assert.equal(study.getScopeModel('PATIENT').workflowIds[0], 'WORKFLOW', 'Workflow is added to scope model');
		});

		//add workflow to event model
		await feature.it('links workflow to event model', async () => {
			await driver.click('#tree ul.event_model > li a[href="#node=Study:TEST|ScopeModel:PATIENT|EventModel:UNEXPECTED"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#event_model_workflow_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_event_model_form');

			const event_model = study.getScopeModel('PATIENT').getEventModel('UNEXPECTED');
			assert.equal(event_model.workflowIds[0], 'WORKFLOW', 'Workflow is added to event model');
		});

		//add workflow to form model
		await feature.it('links workflow to form model', async () => {
			await driver.click('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:DEMOGRAPHICS"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#form_model_workflow_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_form_model_form');

			assert.equal(study.getFormModel('DEMOGRAPHICS').workflowIds[0], 'WORKFLOW', 'Workflow is added to form model');
		});

		//add workflow to field model
		await feature.it('links workflow to field model', async () => {
			await driver.click('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS|FieldModel:STREET"]');
			await driver.wait();
			await driver.click('a[href="#node=Study:TEST|DatasetModel:ADDRESS|FieldModel:STREET&tab=edit_field_model_validators_workflows"]');
			await driver.wait();
			await driver.doubleClick(await driver.getShadow('#field_model_workflow_ids', 'div > ul > li:nth-child(1)'));
			await driver.submit('#edit_field_model_form');

			assert.equal(study.getDatasetModel('ADDRESS').getFieldModel('STREET').workflowIds[0], 'WORKFLOW', 'Workflow is added to field model');
		});

		//add field model to form model
		await feature.it('links field model to form model', async () => {
			await driver.click('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:DEMOGRAPHICS"]');
			await driver.wait();
			await driver.click('#form_model_layout_add');
			await driver.dragAndDrop('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION|FieldModel:DATE_OF_BIRTH"]', '#form_model_layouts > div > table > tbody > tr > td:nth-child(2)');
			await driver.submit('#edit_form_model_form');

			await driver.click('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:LOCATION"]');
			await driver.wait();
			await driver.click('#form_model_layout_add');
			await driver.dragAndDrop('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS|FieldModel:STREET"]', '#form_model_layouts > div > table > tbody > tr > td:nth-child(2)');
			await driver.submit('#edit_form_model_form');

			assert.equal(study.getFormModel('LOCATION').layouts.length, 1, 'A layout is created with the form model');
			assert.equal(study.getFormModel('LOCATION').layouts[0].lines[0].cells[0].datasetModelId, 'ADDRESS', 'Form model cell is filled with good field model');
			assert.equal(study.getFormModel('LOCATION').layouts[0].lines[0].cells[0].fieldModelId, 'STREET', 'Form model cell is filled with good field model');
		});
	});

	await bundle.describe('modifications', async feature => {
		//edit language de
		await feature.it('edits language (FS_LANGUAGE_002)', async () => {
			await driver.click('#tree ul.language > li a[href="#node=Study:TEST|Language:de"]');
			await driver.wait();
			await driver.type('#language_id', 'es');
			await driver.type(await driver.getShadow('#language_shortname', 'input'), 'Spanish');
			await driver.submit('#edit_language_form');

			assert.equal(study.languages.last().id, 'es', 'Language is modified');
			assert.equal(study.languages.last().shortname['en'], 'Spanish', 'Language shortname is modified');
		});

		//edit profile NURSE
		await driver.click('#tree ul.profile > li a[href="#node=Study:TEST|Profile:NURSE"]');
		await driver.wait();
		await driver.type('#profile_id', 'PRINCIPAL_NURSE');
		await driver.type(await driver.getShadow('#profile_shortname', 'input'), 'Principal nurse');
		await driver.submit('#edit_profile_form');

		await feature.it('edits profile (FS_PROFILE_002)', async () => {
			assert.equal(study.profiles.last().id, 'PRINCIPAL_NURSE', 'Profile id is modified');
			assert.equal(study.profiles.last().shortname['en'], 'Principal nurse', 'Profile shortname is modified');
		});

		await feature.it('edits profile and updates other profiles (FS_PROFILE_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedProfileIdRights.hasOwnProperty('NURSE'), 'Profile has right on other profile');
			assert.ok(study.getProfile('INVESTIGATOR').grantedProfileIdRights.hasOwnProperty('PRINCIPAL_NURSE'), 'Profile has right on other profile');
		});

		//edit feature TEST_FEATURE
		await driver.click('a[href="#node=Study:TEST|Feature:TEST_FEATURE"]');
		await driver.wait();
		await driver.type('#feature_id', 'TEST_FEATURE_MODIFIED');
		await driver.type(await driver.getShadow('#feature_shortname', 'input'), 'Test feature modified');
		await driver.submit('#edit_feature_form');

		await feature.it('edits feature (FS_FEATURE_002)', async () => {
			assert.equal(study.features.last().id, 'TEST_FEATURE_MODIFIED', 'Feature id is modified');
			assert.equal(study.features.last().shortname['en'], 'Test feature modified', 'Feature shortname is modified');
		});

		await feature.it('edits feature and updates profiles (FS_FEATURE_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedFeatureIds.includes('TEST_FEATURE'), 'Profile has right on updated feature');
			assert.ok(study.getProfile('INVESTIGATOR').grantedFeatureIds.includes('TEST_FEATURE_MODIFIED'), 'Profile has right on updated feature');
		});

		//edit menu
		await driver.click('#tree ul.menu > li a[href="#node=Study:TEST|Menu:HOME|Menu:DASHBOARD"]');
		await driver.wait();
		await driver.type('#menu_id', 'DASHBOARD_MODIFIED');
		await driver.type(await driver.getShadow('#menu_shortname', 'input'), 'Dashboard modified');
		await driver.submit('#edit_menu_form');

		await feature.it('edits menu (FS_MENU_002)', async () => {
			assert.equal(study.getMenu('HOME').submenus[0].id, 'DASHBOARD_MODIFIED', 'Menu id is changed');
			assert.equal(study.getMenu('HOME').submenus[0].parent.id, 'HOME', 'Menu is still the child of the same menu');
		});

		await feature.it('edits menu and updates profiles (FS_MENU_003)', async () => {
			assert.equal(study.getProfile('INVESTIGATOR').grantedMenuIds.length, 2, 'Menus have been given to a profile');
			assert.ok(study.getProfile('INVESTIGATOR').grantedMenuIds.includes('HOME'), 'Profile has right on updated menu');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedMenuIds.includes('DASHBOARD'), 'Profile has right on updated menu');
			assert.ok(study.getProfile('INVESTIGATOR').grantedMenuIds.includes('DASHBOARD_MODIFIED'), 'Profile has right on updated menu');
		});

		//edit action ACTION
		await driver.click('#tree ul.action > li a[href="#node=Study:TEST|Workflow:WORKFLOW|Action:ACTION"]');
		await driver.wait();
		await driver.type('#action_id', 'ACTION_MODIFIED');
		await driver.type(await driver.getShadow('#action_shortname', 'input'), 'Action modified');
		await driver.submit('#edit_action_form');

		await feature.it('edits action (FS_ACTION_002)', async () => {
			assert.equal(study.getWorkflow('WORKFLOW').actions.last().id, 'ACTION_MODIFIED', 'Action id is modified');
			assert.equal(study.getWorkflow('WORKFLOW').actions.last().shortname['en'], 'Action modified', 'Action shortname is modified');
		});

		await feature.it('edits action and updates profiles (FS_ACTION_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedWorkflowIds['WORKFLOW'].childRights.hasOwnProperty('ACTION'), 'Profile has right on updated action');
			assert.ok(study.getProfile('INVESTIGATOR').grantedWorkflowIds['WORKFLOW'].childRights.hasOwnProperty('ACTION_MODIFIED'), 'Profile has right on updated action');
		});

		await feature.it('edits action and updates workflows (FS_ACTION_004)', async () => {
			assert.equal(study.getWorkflow('WORKFLOW').actionId, 'ACTION_MODIFIED', 'Workflow is linked to updated action');
		});

		await feature.it('edits action and updates workflow states (FS_ACTION_005)', async () => {
			assert.notOk(study.getWorkflow('WORKFLOW').getState('STATE').possibleActionIds.includes('ACTION'), 'Workflow state is linked to updated action');
			assert.ok(study.getWorkflow('WORKFLOW').getState('STATE').possibleActionIds.includes('ACTION_MODIFIED'), 'Workflow state is linked to updated action');
		});

		//edit workflow state STATE
		await driver.click('#tree ul.workflow_state > li a[href="#node=Study:TEST|Workflow:WORKFLOW|WorkflowState:STATE"]');
		await driver.wait();
		await driver.type('#workflow_state_id', 'STATE_MODIFIED');
		await driver.type(await driver.getShadow('#workflow_state_shortname', 'input'), 'State modified');
		await driver.submit('#edit_workflow_state_form');

		await feature.it('edits workflow state (FS_WORKFLOW_STATE_002)', async () => {
			assert.equal(study.getWorkflow('WORKFLOW').states.last().id, 'STATE_MODIFIED', 'Workflow state id is modified');
			assert.equal(study.getWorkflow('WORKFLOW').states.last().shortname['en'], 'State modified', 'Workflow state shortname is modified');
		});

		//edit workflow
		await driver.click('#tree ul.workflow > li a[href="#node=Study:TEST|Workflow:WORKFLOW"]');
		await driver.wait();
		await driver.type('#workflow_id', 'WORKFLOW_MODIFIED');
		await driver.type(await driver.getShadow('#workflow_shortname', 'input'), 'Workflow modified');
		await driver.submit('#edit_workflow_form');

		await feature.it('edits workflow (FS_WORKFLOW_002)', async () => {
			assert.equal(study.workflows.last().id, 'WORKFLOW_MODIFIED', 'Workflow is modified');
			assert.equal(study.workflows.last().shortname['en'], 'Workflow modified', 'Workflow shortname is modified');
		});

		await feature.it('edits workflow and updates profiles (FS_WORKFLOW_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedWorkflowIds.hasOwnProperty('WORKFLOW'), 'Profile has right on modified workflow');
			assert.ok(study.getProfile('INVESTIGATOR').grantedWorkflowIds.hasOwnProperty('WORKFLOW_MODIFIED'), 'Profile has right on modified workflow');
			assert.ok(study.getProfile('INVESTIGATOR').grantedWorkflowIds['WORKFLOW_MODIFIED'].childRights.hasOwnProperty('ACTION_MODIFIED'), 'Profile has right on action');
		});

		await feature.it('edits workflow and updates scope models (FS_WORKFLOW_004)', async () => {
			assert.notOk(study.getScopeModel('PATIENT').workflowIds.includes('WORKFLOW'), 'Scope model is linked to modified workflow');
			assert.ok(study.getScopeModel('PATIENT').workflowIds.includes('WORKFLOW_MODIFIED'), 'Scope model is linked to modified workflow');
		});

		await feature.it('edits workflow and updates event models (FS_WORKFLOW_005)', async () => {
			const event_model = study.getScopeModel('PATIENT').getEventModel('UNEXPECTED');
			assert.notOk(event_model.workflowIds.includes('WORKFLOW'), 'Event model is linked to modified workflow');
			assert.ok(event_model.workflowIds.includes('WORKFLOW_MODIFIED'), 'Event model is linked to modified workflow');
		});

		await feature.it('edits workflow and updates form models (FS_WORKFLOW_006)', async () => {
			assert.notOk(study.getFormModel('DEMOGRAPHICS').workflowIds.includes('WORKFLOW'), 'Form model is linked to modified workflow');
			assert.ok(study.getFormModel('DEMOGRAPHICS').workflowIds.includes('WORKFLOW_MODIFIED'), 'Form model is linked to modified workflow');
		});

		await feature.it('edits workflow and updates field models (FS_WORKFLOW_007)', async () => {
			assert.notOk(study.getDatasetModel('ADDRESS').getFieldModel('STREET').workflowIds.includes('WORKFLOW'), 'Field model is linked to modified workflow');
			assert.ok(study.getDatasetModel('ADDRESS').getFieldModel('STREET').workflowIds.includes('WORKFLOW_MODIFIED'), 'Field model is linked to modified workflow');
		});

		//edit scope model COUNTRY
		await driver.click('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:COUNTRY"]');
		await driver.wait();
		await driver.type('#scope_model_id', 'CONTINENT');
		await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Continent');
		await driver.submit('#edit_scope_model_form');

		//edit scope model PATIENT
		await driver.click('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:PATIENT"]');
		await driver.wait();
		await driver.type('#scope_model_id', 'SUBJECT');
		await driver.type(await driver.getShadow('#scope_model_shortname', 'input'), 'Subject');
		await driver.submit('#edit_scope_model_form');

		await feature.it('edits scope model (FS_SCOPE_MODEL_002)', async () => {
			assert.equal(study.scopeModels[1].id, 'CONTINENT', 'Scope model id is changed');
			assert.equal(study.scopeModels[1].shortname['en'], 'Continent', 'Scope model shortname is modified');

			assert.equal(study.scopeModels[3].id, 'SUBJECT', 'Scope model id is changed');
			assert.equal(study.scopeModels[3].shortname['en'], 'Subject', 'Scope model shortname is modified');
		});

		await feature.it('edits scope model and updates profiles (FS_SCOPE_MODEL_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('COUNTRY'), 'Profile has right on updated scope model');
			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('CONTINENT'), 'Profile has right on updated scope model');

			assert.notOk(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('PATIENT'), 'Profile has right on updated scope model');
			assert.ok(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('SUBJECT'), 'Profile has right on updated scope model');
		});

		await feature.it('edits scope model and updates other scope models (FS_SCOPE_MODEL_004)', async () => {
			assert.notOk(study.getScopeModel('CENTER').parentIds.includes('COUNTRY'), 'Scope model is linked on updated scope model');
			assert.ok(study.getScopeModel('CENTER').parentIds.includes('CONTINENT'), 'Scope model is linked on updated scope model');

			const event_model = study.getScopeModel('SUBJECT').getEventModel('EXPECTED');
			assert.notEqual(event_model.scopeModel.id, 'PATIENT', 'Event model is linked on updated scope model');
			assert.equal(event_model.scopeModel.id, 'SUBJECT', 'Event model is linked on updated scope model');
		});

		//edit event group SCHEDULING
		/*await feature.it('edits event group (FS_EVENT_GROUP_002)', async () => {
			await driver.click('#tree ul.event_group > li a[href="#node=Study:TEST|EventGroup:SCHEDULING"]');
			await driver.wait();
			await driver.type('#event_group_id', 'PLANNING');
			await driver.type(await driver.getShadow('#event_group_shortname', 'input'), 'Planning');
			await driver.submit('#edit_event_group_general');

			assert.equal(study.eventGroups[1].id, 'PLANNING', 'Event group id is modified');
			assert.equal(study.eventGroups[1].shortname['en'], 'Planning', 'Event group shortname is modified');
		});*/

		//edit event model UNEXPECTED
		await driver.click('a[href="#node=Study:TEST|ScopeModel:SUBJECT|EventModel:UNEXPECTED"]');
		await driver.wait();
		await driver.type('#event_model_id', 'UNEXPECTED_MODIFIED');
		await driver.type(await driver.getShadow('#event_model_shortname', 'input'), 'Unexpected modified');
		await driver.submit('#edit_event_model_form');

		await feature.it('edits event model (FS_EVENT_MODEL_002)', async () => {
			const scope_model = study.getScopeModel('SUBJECT');
			assert.equal(scope_model.eventModels.first().id, 'UNEXPECTED_MODIFIED', 'Event model is modified');
			assert.equal(scope_model.eventModels.first().shortname['en'], 'Unexpected modified', 'Event model shortname is modified');
		});

		await feature.it('edits event model and updates profiles (FS_EVENT_MODEL_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedEventModelIdRights.hasOwnProperty('UNEXPECTED'), 'Profile has right on event model');
			assert.ok(study.getProfile('INVESTIGATOR').grantedEventModelIdRights.hasOwnProperty('UNEXPECTED_MODIFIED'), 'Profile has right on event model');
		});

		//edit dataset model PATIENT_DOCUMENT
		await driver.click('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION"]');
		await driver.wait();
		await driver.type('#dataset_model_id', 'PATIENT_DOCUMENTATION_MODIFIED');
		await driver.type(await driver.getShadow('#dataset_model_shortname', 'input'), 'Patient documentation');
		await driver.submit('#edit_dataset_model_form');

		//edit dataset model ADDRESS
		await driver.click('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS"]');
		await driver.wait();
		await driver.type('#dataset_model_id', 'ADDRESS_MODIFIED');
		await driver.type(await driver.getShadow('#dataset_model_shortname', 'input'), 'Address modified');
		await driver.submit('#edit_dataset_model_form');

		await feature.it('edits dataset model (FS_DATASET_MODEL_002)', async () => {
			assert.equal(study.datasetModels[0].id, 'PATIENT_DOCUMENTATION_MODIFIED', 'Dataset model id is modified');
			assert.equal(study.datasetModels[0].shortname['en'], 'Patient documentation', 'Dataset model shortname is modified');

			assert.equal(study.datasetModels[1].id, 'ADDRESS_MODIFIED', 'Dataset model id is modified');
			assert.equal(study.datasetModels[1].shortname['en'], 'Address modified', 'Dataset model shortname is modified');
		});

		await feature.it('edits dataset model and updates profiles (FS_DATASET_MODEL_003)', async () => {
			assert.notOk(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights.hasOwnProperty('PATIENT_DOCUMENTATION'), 'Profile has right on modified dataset model');
			assert.ok(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights.hasOwnProperty('PATIENT_DOCUMENTATION_MODIFIED'), 'Profile has right on modified dataset model');
		});

		await feature.it('edits dataset model and updates scope models (FS_DATASET_MODEL_004)', async () => {
			assert.notEqual(study.getScopeModel('CENTER').datasetModelIds.last(), 'ADDRESS', 'Scope model is linked to modified dataset model');
			assert.equal(study.getScopeModel('CENTER').datasetModelIds.last(), 'ADDRESS_MODIFIED', 'Scope model is linked to modified dataset model');
		});

		await feature.it('edits dataset model and updates event models (FS_DATASET_MODEL_005)', async () => {
			const event_model = study.getScopeModel('SUBJECT').getEventModel('UNEXPECTED_MODIFIED');
			assert.notOk(event_model.datasetModelIds.includes('PATIENT_DOCUMENTATION'), 'Event model is linked to modified dataset model');
			assert.ok(event_model.datasetModelIds.includes('PATIENT_DOCUMENTATION_MODIFIED'), 'Event model is linked to modified dataset model');
		});

		await feature.it('edits dataset model and updates form models (FS_DATASET_MODEL_006)', async () => {
			assert.notEqual(study.getFormModel('DEMOGRAPHICS').layouts[0].lines[0].cells[0].datasetModelId, 'PATIENT_DOCUMENTATION', 'Form model is linked to modified dataset model');
			assert.equal(study.getFormModel('DEMOGRAPHICS').layouts[0].lines[0].cells[0].datasetModelId, 'PATIENT_DOCUMENTATION_MODIFIED', 'Form model is linked to modified dataset model');
		});

		//edit field model DATE_OF_BIRTH
		await driver.click('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION_MODIFIED|FieldModel:DATE_OF_BIRTH"]');
		await driver.wait();
		await driver.type('#field_model_id', 'DATE_OF_BIRTH_MODIFIED');
		await driver.type(await driver.getShadow('#field_model_shortname', 'input'), 'Date of birth modified');
		await driver.submit('#edit_field_model_form');

		await feature.it('edits field model (FS_FIELD_MODEL_002)', async () => {
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION_MODIFIED').fieldModels.last().id, 'DATE_OF_BIRTH_MODIFIED', 'Field model id is modified');
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION_MODIFIED').fieldModels.last().shortname['en'], 'Date of birth modified', 'Field model shortname is modified');
		});

		await feature.it('edits field model and updates form models (FS_FIELD_MODEL_003)', async () => {
			assert.equal(study.getFormModel('DEMOGRAPHICS').layouts[0].lines[0].cells[0].fieldModelId, 'DATE_OF_BIRTH_MODIFIED', 'Form model is linked to modified field model');
		});

		//edit validator VALIDATOR_1
		await driver.click('#tree ul.validator > li a[href="#node=Study:TEST|Validator:VALIDATOR_1"]');
		await driver.wait();
		await driver.type('#validator_id', 'VALIDATOR_MODIFIED');
		await driver.type(await driver.getShadow('#validator_shortname', 'input'), 'Validator modified');
		await driver.submit('#edit_validator_form');

		await feature.it('edits validator (FS_VALIDATOR_002)', async () => {
			assert.equal(study.validators.last().id, 'VALIDATOR_MODIFIED', 'Validator is modified');
			assert.equal(study.validators.last().shortname['en'], 'Validator modified', 'Validator shortname is modified');
		});

		await feature.it('edits validator and updates field models (FS_VALIDATOR_003)', async () => {
			assert.notOk(study.getDatasetModel('PATIENT_DOCUMENTATION_MODIFIED').getFieldModel('DATE_OF_BIRTH_MODIFIED').validatorIds.includes('VALIDATOR'), 'Field model is linked to modified validator');
			assert.ok(study.getDatasetModel('PATIENT_DOCUMENTATION_MODIFIED').getFieldModel('DATE_OF_BIRTH_MODIFIED').validatorIds.includes('VALIDATOR_MODIFIED'), 'Field model is linked to modified validator');
		});

		//edit form model DEMOGRAPHICS
		await driver.click('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:DEMOGRAPHICS"]');
		await driver.wait();
		await driver.type('#form_model_id', 'DEMOGRAPHICS_MODIFIED');
		await driver.type(await driver.getShadow('#form_model_shortname', 'input'), 'Demographics modified');
		await driver.submit('#edit_form_model_form');

		//edit form model LOCATION
		await driver.click('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:LOCATION"]');
		await driver.wait();
		await driver.type('#form_model_id', 'LOCATION_MODIFIED');
		await driver.type(await driver.getShadow('#form_model_shortname', 'input'), 'Location modified');
		await driver.submit('#edit_form_model_form');

		await feature.it('edits form model (FS_FORM_MODEL_002)', async () => {
			assert.equal(study.formModels[0].id, 'DEMOGRAPHICS_MODIFIED', 'Form model is modified');
			assert.equal(study.formModels[0].shortname['en'], 'Demographics modified', 'Form model shortname is modified');

			assert.equal(study.formModels[1].id, 'LOCATION_MODIFIED', 'Form model is modified');
			assert.equal(study.formModels[1].shortname['en'], 'Location modified', 'Form model shortname is modified');
		});

		await feature.it('edits form model and update profiles (FS_FORM_MODEL_003)', async () => {
			assert.equal(study.getProfile('INVESTIGATOR').grantedFormModelIdRights['DEMOGRAPHICS_MODIFIED'][0], 'WRITE','Profile has right on modified form model');
			assert.equal(study.getProfile('INVESTIGATOR').grantedFormModelIdRights['LOCATION_MODIFIED'][0], 'WRITE', 'Profile has right on modified form model');
		});

		await feature.it('edits form model and update scope models (FS_FORM_MODEL_004)', async () => {
			assert.notOk(study.getScopeModel('CENTER').formModelIds.includes('LOCATION'), 'Scope model is linked to modified form model');
			assert.ok(study.getScopeModel('CENTER').formModelIds.includes('LOCATION_MODIFIED'), 'Scope model is linked to modified form model');
		});

		await feature.it('edits form model and update event models (FS_FORM_MODEL_005)', async () => {
			const event_model = study.getScopeModel('SUBJECT').getEventModel('UNEXPECTED_MODIFIED');
			assert.notOk(event_model.formModelIds.includes('DEMOGRAPHICS'), 'Event model is linked to modified form model');
			assert.ok(event_model.formModelIds.includes('DEMOGRAPHICS_MODIFIED'), 'Event model is linked to modified form model');
		});

		//edit workflow widget
		await driver.click('#tree ul.workflow_widget > li a[href="#node=Study:TEST|WorkflowWidget:WORKFLOW_WIDGET_1"]');
		await driver.wait();
		await driver.type('#workflow_widget_id', 'WORKFLOW_WIDGET_MODIFIED');
		await driver.type(await driver.getShadow('#workflow_widget_shortname', 'input'), 'Workflow widget modified');
		await driver.submit('#edit_workflow_widget_form');

		await feature.it('edits workflow widget (FS_WORKFLOW_WIDGET_002)', async () => {
			assert.equal(study.workflowWidgets.last().id, 'WORKFLOW_WIDGET_MODIFIED', 'Workflow widget is modified');
			assert.equal(study.workflowWidgets.last().shortname['en'], 'Workflow widget modified', 'Workflow widget shortname is modified');
		});

		//edit resource category
		await driver.click('#tree ul.resource_category > li a[href="#node=Study:TEST|ResourceCategory:RESOURCE_CATEGORY_1"]');
		await driver.wait();
		await driver.type('#resource_category_id', 'RESOURCE_CATEGORY_MODIFIED');
		await driver.type(await driver.getShadow('#resource_category_shortname', 'input'), 'Resource category modified');
		await driver.submit('#edit_resource_category_form');

		await feature.it('edits resource category (FS_RESOURCE_CATEGORY_002)', async () => {
			assert.equal(study.resourceCategories.last().id, 'RESOURCE_CATEGORY_MODIFIED', 'Resource category id is modified');
			assert.equal(study.resourceCategories.last().shortname['en'], 'Resource category modified', 'Resource category shortname is modified');
		});

		await feature.it('edits resource category and update profiles (FS_RESOURCE_CATEGORY_003)', async () => {
			assert.equal(study.getProfile('INVESTIGATOR').grantedCategoryIds[0], 'RESOURCE_CATEGORY_MODIFIED', 'Profile has right on modified resource category');
		});

		//edit privacy policy
		await driver.click('#tree ul.privacy_policy > li a[href="#node=Study:TEST|PrivacyPolicy:PRIVACY_POLICY_1"]');
		await driver.wait();
		await driver.type('#privacy_policy_id', 'PRIVACY_POLICY_MODIFIED');
		await driver.type(await driver.getShadow('#privacy_policy_shortname', 'input'), 'Privacy policy modified');
		await driver.submit('#edit_privacy_policy_form');

		await feature.it('edits privacy policy (FS_PRIVACY_POLICY_002)', async () => {
			assert.equal(study.privacyPolicies.last().id, 'PRIVACY_POLICY_MODIFIED', 'Privacy policy is modified');
			assert.equal(study.privacyPolicies.last().shortname['en'], 'Privacy policy modified', 'Privacy policy shortname is modified');
		});
	});

	await bundle.describe('deletions', async feature => {
		//delete language
		await driver.contextMenu('#tree ul.language > li a[href="#node=Study:TEST|Language:es"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes language (FS_LANGUAGE_003)', async () => {
			assert.equal(study.languages.length, 2, 'Language is deleted');
			assert.equal(study.languages[0].id, 'en', 'Language is deleted, existing language is unchanged');
			assert.equal(study.languages[1].id, 'fr', 'Language is deleted, existing language is unchanged');
		});

		//delete feature
		const features_number = study.features.length;

		await driver.contextMenu('#tree ul.feature > li a[href="#node=Study:TEST|Feature:TEST_FEATURE_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes feature and updates profiles (FS_FEATURE_004, FS_FEATURE_005)', async () => {
			assert.equal(study.features.length, features_number - 1, 'Feature is deleted');
			assert.equal(study.getProfile('INVESTIGATOR').grantedFeatureIds.length, 0, 'Deleted feature is removed from profile');
		});

		//delete action
		await driver.contextMenu('#tree ul.action > li a[href="#node=Study:TEST|Workflow:WORKFLOW_MODIFIED|Action:ACTION_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes action and updates profiles, workflows and workflow states (FS_ACTION_006, FS_ACTION_007, FS_ACTION_008, FS_ACTION_009)', async () => {
			assert.equal(study.getWorkflow('WORKFLOW_MODIFIED').actions.length, 0, 'Action is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedWorkflowIds['WORKFLOW_MODIFIED'].childRights.hasOwnProperty('ACTION_MODIFIED'), 'Deleted action is removed from profile');
			assert.equal(study.getWorkflow('WORKFLOW_MODIFIED').actionId, undefined, 'Deleted action is removed from workflow');
			assert.notOk(study.getWorkflow('WORKFLOW_MODIFIED').getState('STATE_MODIFIED').possibleActionIds.includes('ACTION_MODIFIED'), 'Deleted action is removed from workflow state');
		});

		//delete workflow state
		await driver.contextMenu('#tree ul.workflow_state > li a[href="#node=Study:TEST|Workflow:WORKFLOW_MODIFIED|WorkflowState:STATE_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes workflow state (FS_WORKFLOW_STATE_003)', async () => {
			assert.equal(study.getWorkflow('WORKFLOW_MODIFIED').states.length, 0, 'Workflow state is deleted');
		});

		//delete workflow
		await driver.contextMenu('#tree ul.workflow > li a[href="#node=Study:TEST|Workflow:WORKFLOW_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes workflow and updates profiles, scope models, event models, form models and field models (FS_WORKFLOW_008, FS_WORKFLOW_009, FS_WORKFLOW_010, FS_WORKFLOW_011, FS_WORKFLOW_012, FS_WORKFLOW_013)', async () => {
			assert.equal(study.workflows.length, 0, 'Workflow is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedWorkflowIds.hasOwnProperty('WORKFLOW_MODIFIED'), 'Deleted workflow is removed from profile');
			assert.notOk(study.getScopeModel('SUBJECT').workflowIds.includes('WORKFLOW_MODIFIED'), 'Deleted workflow is removed from scope model');
			assert.notOk(study.getScopeModel('SUBJECT').getEventModel('UNEXPECTED_MODIFIED').workflowIds.includes('WORKFLOW_MODIFIED'), 'Deleted workflow is removed from event model');
			assert.notOk(study.getFormModel('DEMOGRAPHICS_MODIFIED').workflowIds.includes('WORKFLOW_MODIFIED'), 'Deleted workflow is removed from form model');
			assert.notOk(study.getDatasetModel('ADDRESS_MODIFIED').getFieldModel('STREET').workflowIds.includes('WORKFLOW_MODIFIED'), 'Deleted workflow is removed from field model');
		});

		//delete validator
		await driver.contextMenu('#tree ul.validator > li a[href="#node=Study:TEST|Validator:VALIDATOR_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes validator and updates field models (FS_VALIDATOR_004, FS_VALIDATOR_005)', async () => {
			assert.equal(study.validators.length, 0, 'Validator is deleted');
			assert.equal(study.getDatasetModel('PATIENT_DOCUMENTATION_MODIFIED').getFieldModel('DATE_OF_BIRTH_MODIFIED').validatorIds.length, 0, 'Deleted validator is removed from field model');
		});

		//delete field model STREET
		await driver.contextMenu('#tree ul.field_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS_MODIFIED|FieldModel:STREET"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes field models and updates form models (FS_FIELD_MODEL_004, FS_FIELD_MODEL_005)', async () => {
			assert.equal(study.getDatasetModel('ADDRESS_MODIFIED').fieldModels.length, 0, 'Field model is deleted');
			assert.equal(study.getFormModel('LOCATION_MODIFIED').layouts[0].lines[0].cells[0].datasetModelId, undefined, 'Deleted field model is removed from form model');
			assert.equal(study.getFormModel('LOCATION_MODIFIED').layouts[0].lines[0].cells[0].fieldModelId, undefined, 'Deleted field model is removed from form model');
		});

		//delete dataset model PATIENT_DOCUMENTATION_MODIFIED
		await driver.contextMenu('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:PATIENT_DOCUMENTATION_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes dataset model and updates profiles, event models and form models (FS_DATASET_MODEL_007, FS_DATASET_MODEL_008, FS_DATASET_MODEL_010, FS_DATASET_MODEL_011)', async () => {
			assert.equal(study.datasetModels.length, 1, 'Dataset model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights.hasOwnProperty('PATIENT_DOCUMENTATION'), 'Deleted dataset model is removed from profile');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedDatasetModelIdRights.hasOwnProperty('PATIENT_DOCUMENTATION_MODIFIED'), 'Deleted dataset model is removed from profile');
			assert.notOk(study.getScopeModel('SUBJECT').getEventModel('UNEXPECTED_MODIFIED').datasetModelIds.includes('PATIENT_DOCUMENTATION_MODIFIED'), 'Deleted dataset model is removed from event model');
			assert.equal(study.getFormModel('DEMOGRAPHICS_MODIFIED').layouts[0].lines[0].cells[0].datasetModelId, undefined, 'Deleted dataset model is removed from form model');
			assert.equal(study.getFormModel('DEMOGRAPHICS_MODIFIED').layouts[0].lines[0].cells[0].fieldModelId, undefined, 'Deleted dataset model is removed from form model');
		});

		//delete dataset model ADDRESS_MODIFIED
		await driver.contextMenu('#tree ul.dataset_model > li a[href="#node=Study:TEST|DatasetModel:ADDRESS_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes dataset model and updates scope models (FS_DATASET_MODEL_007, FS_DATASET_MODEL_009)', async () => {
			assert.equal(study.datasetModels.length, 0, 'Dataset model is deleted');
			assert.notOk(study.getScopeModel('CENTER').datasetModelIds.includes('ADDRESS_MODIFIED'), 'Deleted dataset model is removed from scope model');
			assert.notOk(study.getScopeModel('CENTER').datasetModelIds.includes('ADDRESS'), 'Deleted dataset model is removed from scope model');
		});

		//delete form model DEMOGRAPHICS_MODIFIED
		await driver.contextMenu('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:DEMOGRAPHICS_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes form model and updates profiles and event models (FS_FORM_MODEL_006, FS_FORM_MODEL_007, FS_FORM_MODEL_009)', async () => {
			assert.equal(study.formModels.length, 1, 'Form model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedFormModelIdRights.hasOwnProperty('DEMOGRAPHICS_MODIFIED'), 'Deleted form model is removed from profile');
			assert.notOk(study.getScopeModel('SUBJECT').getEventModel('UNEXPECTED_MODIFIED').formModelIds.includes('DEMOGRAPHICS_MODIFIED'), 'Deleted form model is removed from event model');
		});

		//delete form model LOCATION_MODIFIED
		await driver.contextMenu('#tree ul.form_model > li a[href="#node=Study:TEST|FormModel:LOCATION_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes form model and updates profiles and scope models (FS_FORM_MODEL_006, FS_FORM_MODEL_007, FS_FORM_MODEL_008)', async () => {
			assert.equal(study.formModels.length, 0, 'Form model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedFormModelIdRights.hasOwnProperty('LOCATION_MODIFIED'), 'Deleted form model is removed from profile');
			assert.notOk(study.getScopeModel('CENTER').formModelIds.includes('DEMOGRAPHICS_MODIFIED'), 'Deleted form model is removed from scope model');
		});

		//delete event model
		await driver.contextMenu('#tree ul.event_model > li a[href="#node=Study:TEST|ScopeModel:SUBJECT|EventModel:UNEXPECTED_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes event model and updates profiles (FS_EVENT_MODEL_005, FS_EVENT_MODEL_006)', async () => {
			assert.equal(study.getScopeModel('SUBJECT').eventModels.length, 1, 'Event model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedEventModelIdRights.hasOwnProperty('UNEXPECTED_MODIFIED'), 'Deleted event model is removed from profile');
		});

		//delete event group
		/*await driver.contextMenu('#tree ul.event_group > li a[href="#node=Study:TEST|EventGroup:PLANNING"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes event group (FS_EVENT_GROUP_004)', async () => {
			assert.equal(study.eventGroups.length, 1, 'Event group is deleted');
		});*/

		//delete scope model SUBJECT
		await driver.contextMenu('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:SUBJECT"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes scope model and updates other scope models and event models (FS_SCOPE_MODEL_006, FS_SCOPE_MODEL_007)', async () => {
			assert.equal(study.scopeModels.length, 3, 'Scope model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('SUBJECT'), 'Deleted scope model is removed from profile');
		});

		//delete scope model CONTINENT
		await driver.contextMenu('#tree ul.scope_model > li a[href="#node=Study:TEST|ScopeModel:CONTINENT"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes scope model and updates profiles and other scope models (FS_SCOPE_MODEL_006, FS_SCOPE_MODEL_007, FS_SCOPE_MODEL_008)', async () => {
			assert.equal(study.scopeModels.length, 2, 'Scope model is deleted');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedScopeModelIdRights.hasOwnProperty('CONTINENT'), 'Deleted scope model is removed from profile');
			assert.notOk(study.getScopeModel('CENTER').parentIds.includes('CONTINENT'), 'Deleted scope model is no more linked to deleted scope model');
		});

		//delete menu
		await driver.contextMenu('#tree ul.menu > li a[href="#node=Study:TEST|Menu:HOME|Menu:DASHBOARD_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes menu and updates profiles (FS_MENU_004, FS_MENU_005)', async () => {
			assert.equal(study.getMenu('HOME').submenus.length, 0, 'Menu is deleted');
			assert.equal(study.getProfile('INVESTIGATOR').grantedMenuIds.length, 1, 'Menu is deleted from profile');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedMenuIds.includes('DASHBOARD_MODIFIED'), 'Menus is deleted from profile');
			assert.ok(study.getProfile('INVESTIGATOR').grantedMenuIds.includes('HOME'), 'Menus is deleted from profile');
		});

		//delete resource category
		const resource_categories_number = study.resourceCategories.length;

		await driver.contextMenu('#tree ul.resource_category > li a[href="#node=Study:TEST|ResourceCategory:RESOURCE_CATEGORY_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes menu and updates profiles (FS_RESOURCE_CATEGORY_004, FS_RESOURCE_CATEGORY_005)', async () => {
			assert.equal(study.resourceCategories.length, resource_categories_number - 1, 'Resource category is deleted');
			assert.equal(study.getProfile('INVESTIGATOR').grantedCategoryIds.length, 0, 'Resource category is deleted from profile');
		});

		//delete workflow widget
		await driver.contextMenu('#tree ul.workflow_widget > li a[href="#node=Study:TEST|WorkflowWidget:WORKFLOW_WIDGET_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes workflow widget (FS_WORKFLOW_WIDGET_003)', async () => {
			assert.equal(study.workflowWidgets.length, 0, 'Workflow widget is deleted');
		});

		//delete privacy policy
		await driver.contextMenu('#tree ul.privacy_policy > li a[href="#node=Study:TEST|PrivacyPolicy:PRIVACY_POLICY_MODIFIED"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes privacy policy (FS_PRIVACY_POLICY_003)', async () => {
			assert.equal(study.privacyPolicies.length, 0, 'Privacy policy is deleted');
		});

		//delete profile
		await driver.contextMenu('#tree ul.profile > li a[href="#node=Study:TEST|Profile:PRINCIPAL_NURSE"]');
		await driver.wait();
		await driver.click('#node_menu_delete');
		await driver.click('#validate_buttons > li:last-child > button');

		await feature.it('deletes profile and updates other profiles (FS_PROFILE_004, FS_PROFILE_005)', async () => {
			assert.equal(study.profiles.length, 1, 'Profile is deleted');
			assert.equal(study.profiles[0].id, 'INVESTIGATOR', 'Profile is deleted, existing profile is unchanged');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedProfileIdRights.hasOwnProperty('NURSE'), 'Deleted profile is removed from other profile');
			assert.notOk(study.getProfile('INVESTIGATOR').grantedProfileIdRights.hasOwnProperty('PRINCIPAL_NURSE'), 'Deleted profile is removed from other profile');
		});
	});

	bundle.end();
}
