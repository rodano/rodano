import '../../../basic-tools/extension.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {DisplayableNode} from '../node_displayable.js';
import {Study} from './study.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

export class FormModel extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			layouts: {type: 'array', subtype: Entities.Layout.name},
			workflowIds: {type: 'array'},
			rules: {type: 'array', subtype: Entities.Rule.name},
			constraint: {type: Entities.RuleConstraint.name},
			optional: {type: 'boolean'},
			printButtonLabel: {type: 'object'},
			xslTemplate: {type: 'string'},
			xslFilename: {type: 'string'}
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.FORM
	];

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.layouts = [];
		this.workflowIds = [];
		this.rules = [];
		this.constraint = undefined;
		this.optional = false;
		this.printButtonLabel = {};
		this.xslTemplate = undefined;
		this.xslFilename = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
	getLayout(layout_id) {
		return Utils.getObjectById(this.layouts, layout_id);
	}
	getCells() {
		return this.layouts.flatMap(l => l.getCells());
	}
	getCell(cell_id) {
		return Utils.getObjectById(this.getCells(), cell_id);
	}
	getWorkflows() {
		return this.workflowIds.map(Study.prototype.getWorkflow, this.study);
	}
	getScopeModels() {
		return this.study.scopeModels.filter(s => s.formModelIds.includes(this.id));
	}
	getEventModels() {
		return this.study.getEventModels().filter(e => e.formModelIds.includes(this.id));
	}
	getDatasetModels() {
		const dataset_models = [];
		const cells = this.getCells();
		for(let i = 0; i < cells.length; i++) {
			const cell = cells[i];
			if(cell.hasDatasetModel()) {
				const doc = cell.getDatasetModel();
				if(!dataset_models.includes(doc)) {
					dataset_models.push(doc);
				}
				//add master dataset model
				if(doc.family && !doc.master) {
					const master_dataset_model = doc.getMasterDatasetModel();
					if(!dataset_models.includes(master_dataset_model)) {
						dataset_models.push(master_dataset_model);
					}
				}
			}
		}
		return dataset_models;
	}
	getFieldModels() {
		return this.getCells().filter(c => c.hasFieldModel()).map(c => c.getFieldModel());
	}


	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.Layout:
				return this.layouts.slice();
			case Entities.Rule:
				return this.rules.slice();
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.Layout:
				this.layouts.push(child);
				child.formModel = this;
				break;
			case Entities.Rule:
				this.rules.push(child);
				child.rulable = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
	getRelations(entity) {
		switch(entity) {
			case Entities.ScopeModel:
				return this.getScopeModels();
			case Entities.EventModel:
				return this.getEventModels();
			case Entities.Workflow:
				return this.getWorkflows();
			case Entities.DatasetModel:
				return this.getDatasetModels();
			case Entities.FieldModel:
				return this.getFieldModels();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onChangeWorkflowId(event) {
		this.workflowIds.replace(event.oldValue, event.newValue);
	}
	onDeleteWorkflow(event) {
		this.workflowIds.removeElement(event.node.id);
	}

	onMoveLayout(event) {
		if(event.newParent === this) {
			event.node.formModel.layouts.removeElement(event.node);
			event.node.formModel = this;
			this.layouts.push(event.node);
		}
	}
	onDeleteLayout(event) {
		this.layouts.removeElement(event.node);
	}

	onDeleteRule(event) {
		this.rules.removeElement(event.node);
	}

	report(settings) {
		const report = super.report(settings);
		//unused
		if(!this.isUsed()) {
			report.addInfo(`Form model ${this.id} is unused`, this, this['delete'], 'Delete form model');
		}
		//do some check on cells that are not in a constrained layouts
		//some layouts may have commons attributes but not be displayed at the same time according to their constraint
		const cell_data = {};
		const cell_ids = [];
		this.layouts.filter(l => Object.isEmpty(l.constraint)).flatMap(l => l.getCells()).forEach(function(cell) {
			//check uniqueness of cell id
			if(cell_ids.includes(cell.id)) {
				report.addError(
					`Form model ${this.id} contains at least two cells with id ${cell.id}`,
					cell,
					function() {
						const line = this.line;
						const layout = line.layout;
						this.id = `${layout.id}_${layout.lines.indexOf(line)}_${line.cells.indexOf(this)}`;
					},
					'Change cell id'
				);
			}
			cell_ids.push(cell.id);
			//check uniqueness of field model and dataset model couple
			if(cell.hasFieldModel()) {
				try {
					//check field model is valid
					cell.getFieldModel();
					//add dataset model as a key
					if(!cell_data.hasOwnProperty(cell.datasetModelId)) {
						cell_data[cell.datasetModelId] = {};
					}
					//check attribute is not already a key
					if(cell_data[cell.datasetModelId].hasOwnProperty(cell.fieldModelId)) {
						report.addError(`Form model ${this.id} contains cell ${cell.id} and cell ${cell_data[cell.datasetModelId][cell.fieldModelId]} which both contain field model ${cell.fieldModelId}`);
					}
					cell_data[cell.datasetModelId][cell.fieldModelId] = cell.id;
				}
				catch(exception) {
					report.addError(exception.message, cell);
				}
			}
		}, this);
		//do some check with all cells
		const event_models = this.getEventModels();
		const scope_models = this.getScopeModels();
		this.getCells().forEach(function(cell) {
			//check event integrity
			if(cell.hasFieldModel()) {
				//cell refers a dataset model which is associated to the event or to the scope model of the event
				const event_invalid = !event_models.every(e => e.datasetModelIds.includes(cell.datasetModelId) || e.scopeModel.datasetModelIds.includes(cell.datasetModelId));
				if(event_invalid) {
					const event_model_ids = event_models.map(e => e.id);
					report.addError(`Form model ${this.id} which contains cell ${cell.id} referring dataset model ${cell.datasetModelId} is used on event models [${event_model_ids}] but one of these event models is not associated with dataset model ${cell.datasetModelId}`);
				}
				//cell refers a dataset model which is associated to the scope model
				const scope_model_invalid = !scope_models.every(s => s.datasetModelIds.includes(cell.datasetModelId));
				if(scope_model_invalid) {
					const scope_model_ids = scope_models.map(s => s.id);
					report.addError(`Form model ${this.id} which contains cell ${cell.id} referring dataset model ${cell.datasetModelId} is used on scope models [${scope_model_ids}] but one of these scope models is not associated with dataset model ${cell.datasetModelId}`);
				}
			}
		}, this);
		return report;
	}

	//html
	createHTML(languages, datasets, objects) {
		return document.createElement('div').appendChildren(this.layouts.map(l => l.createHTML(languages, datasets, objects)));
	}
}
