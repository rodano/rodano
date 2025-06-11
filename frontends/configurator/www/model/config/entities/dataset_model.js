import '../../../basic-tools/extension.js';

import {FieldModelType} from '../field_model_type.js';
import {ComparatorUtils} from '../comparator_utils.js';
import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';
import {RuleEntities} from '../rule_entities.js';
import {Utils} from '../utils.js';
import {Cell} from './cell.js';
import {Column} from './column.js';
import {Layout} from './layout.js';
import {Line} from './line.js';

export class DatasetModel extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			multiple: {type: 'boolean'},
			collapsedLabelPattern: {type: 'string'},
			expandedLabelPattern: {type: 'string'},
			family: {type: 'string'},
			master: {type: 'boolean'},
			exportable: {type: 'boolean'},
			exportOrder: {type: 'number'},
			deleteRules: {type: 'array', subtype: Entities.Rule.name},
			restoreRules: {type: 'array', subtype: Entities.Rule.name},
			fieldModels: {type: 'array', subtype: Entities.FieldModel.name}
		};
	}
	static getExportComparator() {
		return (d1, d2) => ComparatorUtils.compareFields(d1, d2, ['exportOrder', 'id']);
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.DATASET
	];

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.multiple = false;
		this.collapsedLabelPattern = undefined;
		this.expandedLabelPattern = undefined;
		this.family = undefined;
		this.master = false;
		this.exportable = true;
		this.exportOrder = undefined;
		this.deleteRules = [];
		this.restoreRules = [];
		this.fieldModels = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getScopeModels() {
		return this.study.scopeModels.filter(s => s.datasetModelIds.includes(this.id));
	}
	getEventModels() {
		return this.study.getEventModels().filter(e => e.datasetModelIds.includes(this.id));
	}
	getFieldModel(field_model_id) {
		return Utils.getObjectById(this.fieldModels, field_model_id);
	}
	getExportableFieldModels() {
		return this.fieldModels.filter(f => f.exportable);
	}
	getMasterDatasetModel() {
		if(this.family && !this.master) {
			const master_dataset_model = this.study.datasetModels.find(d => d.family === this.family && d.master);
			if(master_dataset_model) {
				return master_dataset_model;
			}
			else {
				throw new Error(`No master for dataset model ${this.id}`);
			}
		}
		throw new Error(`Dataset model ${this.id} is not in a family or is the master of its family`);
	}
	generateLayout(form_model) {
		const layout = new Layout();
		layout.id = `${this.id}_LAYOUT`;
		layout.description = Object.clone(this.shortname);

		const column = new Column({
			cssCode: 'width: 640px;'
		});
		column.layout = layout;
		layout.columns.push(column);

		layout.lines = this.fieldModels.map(function(field_model) {
			//line
			const line = new Line();
			line.layout = layout;

			//cell
			const cell = new Cell({
				id: field_model.generateCellId(form_model),
				datasetModelId: field_model.datasetModel.id,
				fieldModelId: field_model.id,
				cssCodeForLabel: 'width: 320px;'
			});
			cell.line = line;
			line.cells.push(cell);

			return line;
		});

		return layout;
	}
	generateFieldModelId() {
		let index = this.fieldModels.length;
		let field_model_id;
		let valid;
		while(!valid) {
			field_model_id = `${this.id}_${index}`;
			valid = !this.fieldModels.some(f => f.id === field_model_id);
			index++;
		}
		return field_model_id;
	}
	createSQLCreate(format) {
		const table_name = this.id.toLowerCase();
		const indent = '  ';
		const sql = [];
		sql.push(`create table ${table_name} (`);
		sql.push(`${indent}scope_fk int(11),`);
		sql.push(`${indent}id varchar(200),`);
		sql.push(`${indent}code varchar(20),`);
		sql.push(`${indent}scope_model_id varchar(200),`);
		sql.push(`${indent}event_group varchar(100),`);
		sql.push(`${indent}event varchar(100),`);
		sql.push(`${indent}event_group_number int(11),`);
		sql.push(`${indent}event_blocking boolean,`);
		sql.push(`${indent}event_date datetime,`);
		sql.push(`${indent}event_end_date datetime,`);
		sql.push(`${indent}last_update_time datetime,`);
		sql.push(`${indent}dataset_fk int(11),`);
		const field_models = this.getExportableFieldModels().sort((a1, a2) => a1.exportOrder - a2.exportOrder);
		field_models.forEach(function(field_model) {
			const field_model_type = FieldModelType[field_model.type];
			sql.push([`${indent + field_model.id.toLowerCase()} ${field_model_type.sql(field_model.getMaxLength())}, `]);
			if(field_model_type.has_multiple_values) {
				field_model.possibleValues.forEach(function(possible_value) {
					sql.push(`${indent + possible_value.id.toLowerCase()} varchar(5),`);
					if(possible_value.specify) {
						sql.push(`${indent + possible_value.id.toLowerCase()}_specify varchar(400),`);
					}
				});
			}
		});
		sql.push(`${indent}constraint pk_${table_name} primary key(dataset_fk)`);
		sql.push(') engine=InnoDB character set utf8 collate utf8_bin;');
		return sql.join(format || ' ');
	}
	createSQLDrop() {
		return `drop table if exists ${this.id.toLowerCase()};`;
	}

	//rulable and layoutable
	getStudy() {
		return this.study;
	}

	//tree
	getChildren(entity, index) {
		switch(entity) {
			case Entities.FieldModel:
				return this.fieldModels.slice();
			case Entities.Rule:
				return index === 0 ? this.deleteRules.slice() : this.restoreRules.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child, index) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.FieldModel:
				this.fieldModels.push(child);
				child.datasetModel = this;
				break;
			case Entities.Rule:
				index === 0 ? this.deleteRules.push(child) : this.restoreRules.push(child);
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
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onDeleteFieldModel(event) {
		this.fieldModels.removeElement(event.node);
	}
	onMoveFieldModel(event) {
		if(event.newParent === this) {
			event.node.datasetModel.fieldModels.removeElement(event.node);
			event.node.datasetModel = this;
			this.fieldModels.push(event.node);
		}
	}
	onDeleteRule(event) {
		this.deleteRules.removeElement(event.node);
		this.restoreRules.removeElement(event.node);
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//dataset model must not be empty
		if(this.fieldModels.isEmpty()) {
			report.addError(`Dataset model ${this.id} does not have any field models`);
		}
		if(this.multiple && !this.collapsedLabelPattern) {
			report.addError(`Dataset model ${this.id} is multiple but does not have a collapsed label pattern`, this);
		}
		//check uniqueness of columns (field model ids and their possible value ids for field models that can have multiple values)
		const columns = [];
		this.fieldModels.forEach(function(field_model) {
			//check field model id
			if(columns.includes(field_model.id)) {
				report.addError(`Field model ${field_model.id} in dataset model ${this.id} has an id which is already used by an other field model or a possible value of the same dataset model`, field_model);
			}
			columns.push(field_model.id);
			//check field model possible value ids
			if(FieldModelType[field_model.type].has_multiple_values) {
				const possible_value_ids = field_model.possibleValues.map(pv => pv.id);
				if(possible_value_ids.includesOne(columns)) {
					report.addError(`Field model ${field_model.id} in dataset model ${this.id} contains a possible value which is already used by an other field model or a possible value of the same dataset model`, field_model);
				}
				columns.pushAll(possible_value_ids);
			}
		}, this);
		return report;
	}
}
