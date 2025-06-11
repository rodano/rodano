import '../../basic-tools/extension.js';

import {Entities} from './entities.js';
import {Action} from './entities/action.js';
import {FieldModel} from './entities/field_model.js';
import {Cell} from './entities/cell.js';
import {DatasetModel} from './entities/dataset_model.js';
import {EventModel} from './entities/event_model.js';
import {Layout} from './entities/layout.js';
import {Line} from './entities/line.js';
import {Menu} from './entities/menu.js';
import {FormModel} from './entities/form_model.js';
import {Rule} from './entities/rule.js';
import {ScopeModel} from './entities/scope_model.js';
import {Study} from './entities/study.js';
import {Validator} from './entities/validator.js';
import {Workflow} from './entities/workflow.js';
import {Node} from './node.js';

export default async function test(bundle, assert) {
	bundle.begin();

	//test constructors
	await bundle.describe('Node#constructor', async feature => {
		await feature.it('handles back references properly', () => {
			assert.ok(Node.getBackReferences(Study).isEmpty(), 'Entity "Study" does not have any back reference');

			assert.equal(Node.getBackReferences(ScopeModel).length, 1, 'Entity "ScopeModel" has only one back reference');
			assert.equal(Node.getBackReferences(ScopeModel)[0], 'study', 'Entity "ScopeModel" has a back reference named "study"');

			assert.equal(Node.getBackReferences(FieldModel).length, 1, 'Entity "FieldModel" has only one back reference');
			assert.equal(Node.getBackReferences(FieldModel)[0], 'datasetModel', 'Entity "FieldModel" has a back reference named "datasetModel"');
		});
	});

	await bundle.describe('Entities', async feature => {
		await feature.it('finds a proper path between two entities', () => {
			assert.equal(Entities.Study.getPath(Entities.Workflow).length, 1, 'Path between entity "Study" and entity "Workflow" contains 1 step');
			assert.equal(Entities.Study.getPath(Entities.Workflow)[0], Entities.Workflow, 'Step 1 of path between entity "Study" and entity "Workflow" is entity "Workflow"');

			assert.equal(Entities.Study.getPath(Entities.Action).length, 2, 'Path between entity "Study" and entity "Action" contains 2 steps');
			assert.equal(Entities.Study.getPath(Entities.Action)[0], Entities.Workflow, 'Step 1 of path between entity "Study" and entity "Action" is entity "Workflow"');
			assert.equal(Entities.Study.getPath(Entities.Action)[1], Entities.Action, 'Step 2 of path between entity "Study" and entity "Action" is entity "Action"');
		});
	});

	await bundle.describe('Node#hasParent and Node#getParent', async feature => {
		await feature.it('retrieves parent properly', () => {
			//study
			const study = new Study();
			study.id = 'TEST';

			//scope model
			const scope_model = new ScopeModel();
			scope_model.id = 'PATIENT';

			assert.notOk(study.hasParent(), 'Study does not have a parent');
			assert.ok(scope_model.hasParent(), 'Scope model has a parent');

			assert.ok(Function.isFunction(study.getParent), 'Entity "Study" does have a "getParent" method');
			assert.ok(Function.isFunction(scope_model.getParent), 'Entity "ScopeModel" has a "getParent" method');

			assert.doesThrow(() => scope_model.getParent(), exception => exception.message === 'Parent has not been set yet', 'Scope model parent is undefined while the scope model has not been linked to the study');

			scope_model.study = study;
			study.scopeModels.push(scope_model);

			assert.equal(scope_model.getParent(), study, 'Scope model parent is study');
		});
	});

	await bundle.describe('Node#getGlobalId', async feature => {
		await feature.it('returns the proper global id', () => {
			//study
			const study = new Study();
			study.id = 'TEST';

			assert.equal(study.getGlobalId(), 'Study:TEST', 'Study global id is "Study:TEST"');

			//scope model
			const scope_model = new ScopeModel();
			scope_model.id = 'PATIENT';

			assert.equal(scope_model.getGlobalId(), 'ScopeModel:PATIENT', 'Scope model global id is "ScopeModel:PATIENT" as scope model is not linked to study yet');
			assert.equal(scope_model.getGlobalId(scope_model), 'ScopeModel:PATIENT', 'Scope model related id to itself is "ScopeModel:PATIENT"');

			scope_model.study = study;
			study.scopeModels.push(scope_model);

			assert.equal(scope_model.getGlobalId(), 'Study:TEST|ScopeModel:PATIENT', 'Scope model global id is "Study:TEST|ScopeModel:PATIENT"');
			assert.equal(scope_model.getGlobalId(study), 'Study:TEST|ScopeModel:PATIENT', 'Scope model global id is "Study:TEST|ScopeModel:PATIENT"');
			assert.equal(
				study.getNode(scope_model.getGlobalId()),
				scope_model,
				'Retrieve node with scope model global id gives same scope model');
			assert.equal(scope_model.getGlobalId(), scope_model.getGlobalId(study), 'Scope model global id is the same than scope model related id to its study');
			assert.equal(scope_model.getGlobalId(scope_model), 'ScopeModel:PATIENT', 'Scope model related id to itself is "ScopeModel:PATIENT"');
		});
	});

	await bundle.describe('Node#isDescendantOf and Node#isAncestorOf', async feature => {
		await feature.it('checks properly if a node is a descendant or an ancestor of another node', () => {
			//study
			const study = new Study();
			study.id = 'TEST';

			//considers that a node is neither and descendant nor an ancestor of it-self
			assert.notOk(study.isDescendantOf(study), 'A node is not a descendant of itself');
			assert.notOk(study.isAncestorOf(study), 'A node is not a ancestor of itself');

			//scope model
			const scope_model = new ScopeModel();
			scope_model.id = 'PATIENT';

			assert.doesThrow(
				() => scope_model.isDescendantOf(scope_model),
				e => e.message === 'Parent has not been set yet',
				'Scope model is not a descendant of itself'
			);
			assert.doesThrow(
				() => scope_model.isAncestorOf(scope_model),
				e => e.message === 'Parent has not been set yet',
				'Scope model is not an ancestor of itself'
			);

			scope_model.study = study;
			study.scopeModels.push(scope_model);

			assert.notOk(scope_model.isDescendantOf(scope_model), 'Scope model is not a descendant of itself');
			assert.notOk(scope_model.isAncestorOf(scope_model), 'Scope model is not an ancestor of itself');

			assert.ok(scope_model.isDescendantOf(study), 'Scope model is a descendant of study');
			assert.ok(study.isAncestorOf(scope_model), 'Study is an ancestor of scope model');

			assert.notOk(study.isDescendantOf(scope_model), 'Study is not a descendant of scope model');
			assert.notOk(scope_model.isAncestorOf(study), 'Scope model is not an ancestor of study');
		});
	});

	//study
	const study = new Study();
	study.id = 'TEST';

	//scope model
	const scope_model = new ScopeModel();
	scope_model.id = 'PATIENT';

	scope_model.study = study;
	study.scopeModels.push(scope_model);

	//event group
	/*const event_group = new EventGroup();
	event_group.id = 'VISIT_PLAN';
	event_group.study = study;
	study.eventGroups.push(event_group);*/

	//event model
	const event_model = new EventModel();
	event_model.id = 'BASELINE';
	event_model.scopeModel = scope_model;
	scope_model.eventModels.push(event_model);

	//dataset model
	const dataset_model = new DatasetModel();
	dataset_model.id = 'PATIENT_DOCUMENT';
	dataset_model.study = study;
	study.datasetModels.push(dataset_model);

	//deletion rules
	const deletion_rule = new Rule();
	deletion_rule.description = 'Deletion rule';
	deletion_rule.message = 'Ok';
	deletion_rule.rulable = dataset_model;
	dataset_model.deleteRules.push(deletion_rule);

	const deletion_rule_2 = new Rule();
	deletion_rule_2.description = 'Deletion rule';
	deletion_rule_2.message = 'Ok';
	deletion_rule_2.rulable = dataset_model;
	dataset_model.deleteRules.push(deletion_rule_2);

	//restoration rule
	const restoration_rule = new Rule();
	restoration_rule.description = 'Restoration rule';
	restoration_rule.message = 'Ok';
	restoration_rule.rulable = dataset_model;
	dataset_model.restoreRules.push(restoration_rule);

	//field model
	const field_model = new FieldModel();
	field_model.id = 'EMPLOYMENT';

	field_model.datasetModel = dataset_model;
	dataset_model.fieldModels.push(field_model);

	//modification rules
	const modification_rule = new Rule();
	modification_rule.description = 'Modification rule';
	modification_rule.message = 'Ok';
	modification_rule.rulable = field_model;
	field_model.rules.push(modification_rule);

	const modification_rule_2 = new Rule();
	modification_rule_2.description = 'Modification rule';
	modification_rule_2.message = 'Ok';
	modification_rule_2.rulable = field_model;
	field_model.rules.push(modification_rule_2);

	//validator
	const validator = new Validator();
	validator.id = 'MUST_BE_EMPLOYED';
	validator.study = study;
	study.validators.push(validator);

	//menu
	const menu = new Menu();
	menu.id = 'ADMINISTRATION';
	menu.study = study;
	study.menus.push(menu);

	//submenu
	const submenu = new Menu();
	submenu.id = 'USERS';
	submenu.parent = menu;
	menu.submenus.push(submenu);

	//form model
	const form_model = new FormModel();
	form_model.id = 'PATIENT_STATUS';
	form_model.study = study;
	study.formModels.push(form_model);

	//layout
	const layout = new Layout();
	layout.id = 'PATIENT_STATUS_LAYOUT';
	layout.formModel = form_model;
	form_model.layouts.push(layout);

	//line
	const line = new Line();
	line.layout = layout;
	layout.lines.push(line);

	//other line
	const other_line = new Line();
	other_line.layout = layout;
	layout.lines.push(other_line);

	//cell
	const cell = new Cell();
	cell.id = 'EMPLOYMENT';
	cell.line = line;
	line.cells.push(cell);

	//workflow
	const workflow = new Workflow();
	workflow.id = 'REVIEW_PROCESS';

	workflow.study = study;
	study.workflows.push(workflow);

	//action
	const action = new Action();
	action.id = 'REVIEW';

	action.workflow = workflow;
	workflow.actions.push(action);

	await bundle.describe('Node#getRelations and Node#isUsed', async feature => {
		await feature.it('retrieves relations between two nodes', () => {
			assert.doesThrow(
				() => dataset_model.getRelations(Entities.FieldModel),
				e => e.message === 'Entity FieldModel is not related to entity DatasetModel',
				'Entity "DatasetModel" is not related to entity "FieldModel"'
			);
			assert.ok(dataset_model.getRelations(Entities.EventModel).isEmpty(), 'Dataset model is not related to any event yet');
			assert.notOk(dataset_model.isUsed(), 'Dataset model has not been used yet');
			assert.ok(Object.isEmpty(dataset_model.getUsage()), 'Dataset model usage is empty');

			//add dataset model to event model
			event_model.datasetModelIds = [dataset_model.id];

			assert.equal(dataset_model.getRelations(Entities.EventModel).length, 1, 'Dataset model is related to 1 event');
			assert.ok(dataset_model.isUsed(), 'Dataset model is used');
			assert.ok(dataset_model.getUsage().hasOwnProperty(Entities.EventModel.name), 'Dataset model is used by an event');
			assert.equal(dataset_model.getUsage()[Entities.EventModel.name][0], event_model, 'Dataset model is used by the good event');

			assert.doesThrow(
				() => validator.getRelations(Entities.FormModel),
				e => e.message === 'Entity FormModel is not related to entity Validator',
				'Entity "Validator" is not related to entity "FormModel"'
			);
			assert.ok(validator.getRelations(Entities.FieldModel).isEmpty(), 'Validator is not related to any field model yet');
			assert.notOk(validator.isUsed(), 'Validator has not been used yet');
			assert.ok(Object.isEmpty(validator.getUsage()), 'Validator usage is empty');

			//add validator to field model
			field_model.validatorIds = [validator.id];

			assert.equal(validator.getRelations(Entities.FieldModel).length, 1, 'Validator is related to 1 field model');
			assert.ok(validator.isUsed(), 'Validator is used');
			assert.ok(validator.getUsage().hasOwnProperty(Entities.FieldModel.name), 'Validator is used by an field model');
			assert.equal(validator.getUsage()[Entities.FieldModel.name][0], field_model, 'Validator is used by the good field model');

			assert.notOk(field_model.isUsed(), 'Field model has not been used yet');
			assert.ok(Object.isEmpty(field_model.getUsage()), 'Field model usage is empty');

			//add field model to cell
			cell.datasetModelId = 'PATIENT_DOCUMENT';
			cell.fieldModelId = 'EMPLOYMENT';

			assert.ok(field_model.isUsed(), 'Field model is used');
			assert.ok(field_model.getUsage().hasOwnProperty(Entities.FormModel.name), 'Field model is used by a form model');
			assert.equal(field_model.getUsage()[Entities.FormModel.name][0], form_model, 'Field model is used by the good form model');
		});
	});

	await bundle.describe('Node#getGlobalId and Node#getNode', async feature => {
		await feature.it('retrieves a node from its global id', () => {
			const deletion_rule_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-0:0';
			assert.equal(deletion_rule.getGlobalId(), deletion_rule_id, 'First initialisation rule id is "Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-0:0"');
			assert.equal(study.getNode(deletion_rule_id), deletion_rule, 'Retrieve node with global id returns the good rule');

			const deletion_rule_2_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-0:1';
			assert.equal(deletion_rule_2.getGlobalId(), deletion_rule_2_id, 'Second initialisation rule id is "Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-0:1"');
			assert.equal(study.getNode(deletion_rule_2_id), deletion_rule_2, 'Retrieve node with global id returns the good rule');

			const restoration_rule_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-1:0';
			assert.equal(restoration_rule.getGlobalId(), restoration_rule_id, 'First modification rule id is "Study:TEST|DatasetModel:PATIENT_DOCUMENT|Rule-1:0"');
			assert.equal(study.getNode(restoration_rule_id), restoration_rule, 'Retrieve node with global id returns the good rule');

			const field_model_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT';
			assert.equal(study.getNode(field_model_id), field_model, 'Retrieve node with global id returns the good field model');
			assert.equal(field_model.getGlobalId(dataset_model), 'DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT', 'Field model related id to its dataset model is "DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT"');

			const modification_rule_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT|Rule:0';
			assert.equal(modification_rule.getGlobalId(), modification_rule_id, 'First modification rule id is "Study:TEST|DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT|Rule:0"');
			assert.equal(study.getNode(modification_rule_id), modification_rule, 'Retrieve node with global id returns the good rule');

			const modification_rule_2_id = 'Study:TEST|DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT|Rule:1';
			assert.equal(modification_rule_2.getGlobalId(), modification_rule_2_id, 'First modification rule id is "Study:TEST|DatasetModel:PATIENT_DOCUMENT|FieldModel:EMPLOYMENT|Rule:1"');
			assert.equal(study.getNode(modification_rule_2_id), modification_rule_2, 'Retrieve node with global id returns the good rule');

			assert.equal(menu.getGlobalId(), 'Study:TEST|Menu:ADMINISTRATION', 'Menu global id is "Study:TEST|Menu:ADMINISTRATION"');
			assert.equal(submenu.getGlobalId(), 'Study:TEST|Menu:ADMINISTRATION|Menu:USERS', 'Menu global id is "Study:TEST|Menu:ADMINISTRATION|Menu:USERS"');

			assert.equal(form_model.getGlobalId(), 'Study:TEST|FormModel:PATIENT_STATUS', 'Form model global id is "Study:TEST|FormModel:PATIENT_STATUS"');
			assert.equal(layout.getGlobalId(), 'Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT', 'Layout global id is "Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT"');

			assert.equal(line.getGlobalId(), 'Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0', 'Line global id is "Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0"');
			assert.equal(study.getNode(line.getGlobalId()), line, 'Retrieve node with line global id gives same line');
			assert.equal(line.getGlobalId(form_model), 'FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0', 'Line related id to its form model is "FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0"');
			assert.equal(form_model.getNode('FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0'), line, 'Retrieve line in form model with id "FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0" gives good line');

			assert.equal(other_line.getGlobalId(), 'Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:1', 'Line global id is "Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:1"');
			assert.equal(study.getNode(other_line.getGlobalId()), other_line, 'Retrieve node with line global id gives same line');

			assert.equal(cell.getGlobalId(), 'Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0|Cell:EMPLOYMENT', 'Cell global id is "Study:TEST|FormModel:PATIENT_STATUS|Layout:PATIENT_STATUS_LAYOUT|Line:0|Cell:EMPLOYMENT"');
			assert.equal(study.getNode(cell.getGlobalId()), cell, 'Retrieve node with cell global id gives same cell');
			assert.equal(cell.getGlobalId(layout), 'Layout:PATIENT_STATUS_LAYOUT|Line:0|Cell:EMPLOYMENT', 'Cell related id to its layout is "Layout:PATIENT_STATUS_LAYOUT|Line:0|Cell:EMPLOYMENT"');
			assert.equal(layout.getNode('Layout:PATIENT_STATUS_LAYOUT|Line:0|Cell:EMPLOYMENT'), cell, 'Retrieve node in layout with cell related id gives same cell');

			assert.equal(cell.getGlobalId(line), 'Line:0|Cell:EMPLOYMENT', 'Cell related id to its line is "Line:0|Cell:EMPLOYMENT"');
			assert.doesThrow(
				() => cell.getGlobalId(other_line),
				undefined,
				'Cell related id to an other line cannot exist'
			);

			const action_id = 'Study:TEST|Workflow:REVIEW_PROCESS|Action:REVIEW';
			assert.equal(study.getNode(action_id), action, 'Retrieve node with global id returns the good action');
			assert.equal(action.getGlobalId(workflow), 'Workflow:REVIEW_PROCESS|Action:REVIEW', 'Action related id to its workflow is "Workflow:REVIEW_PROCESS|Action:REVIEW"');
		});
	});

	await bundle.describe('Node#getParent and Node#getChild', async feature => {
		await feature.it('retrieves parent and child for a node', () => {
			assert.equal(line.getParent().getChildren(Entities.Line).indexOf(line), 0, 'Line index in its parent is 0');
			assert.equal(layout.getChild(Entities.Line, undefined, 0), line, 'Line is child with id 0 in layout');

			assert.equal(other_line.getParent().getChildren(Entities.Line).indexOf(other_line), 1, 'Line index in its parent 1');
			assert.equal(layout.getChild(Entities.Line, undefined, 1), other_line, 'Line is child with id 1 in layout');
		});
	});

	await bundle.describe('Node#getDescendants', async feature => {
		await feature.it('retrieves descendants for a node', () => {
			assert.ok(field_model.isDescendantOf(dataset_model), 'Field model is a descendant of dataset model');
			assert.ok(field_model.isDescendantOf(study), 'Field model is a descendant of study');

			assert.ok(dataset_model.isAncestorOf(field_model), 'Dataset model is an ancestor of field model');
			assert.ok(study.isAncestorOf(field_model), 'Study is an ancestor of field model');

			assert.notOk(field_model.isDescendantOf(scope_model), 'Field model is not a descendant of scope model');
			assert.notOk(scope_model.isDescendantOf(field_model), 'Scope model is not a descendant of field model');

			assert.equal(study.getDescendants(Entities.FieldModel).length, 1, 'Study contains 1 field model');
			assert.equal(study.getDescendants(Entities.FieldModel)[0], field_model, 'Study contains 1 field model which is the good field model');

			assert.equal(form_model.getDescendants(Entities.Cell).length, 1, 'Form model contains 1 cell');
			assert.equal(form_model.getDescendants(Entities.Cell)[0], cell, 'Form model contains 1 cell which is the good cell');

			assert.ok(action.isDescendantOf(workflow), 'Action is a descendant of workflow');
			assert.ok(action.isDescendantOf(study), 'Action is a descendant of study');

			assert.ok(workflow.isAncestorOf(action), 'Workflow is an ancestor of action');
			assert.ok(study.isAncestorOf(action), 'Study is an ancestor of action');

			assert.notOk(action.isDescendantOf(scope_model), 'Action is not a descendant of scope model');
			assert.notOk(scope_model.isDescendantOf(action), 'Scope model is not a descendant of action');

			assert.equal(study.getDescendants(Entities.Action).length, 1, 'Study contains 1 action');
			assert.equal(study.getDescendants(Entities.Action)[0], action, 'Study contains 1 action which is the good action');
		});
	});

	bundle.end();
}
