import '../../../basic-tools/extension.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {LayoutType} from '../layout_type.js';
import {Line} from './line.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

export class Cell extends Node {
	static getProperties() {
		return {
			line: {type: Entities.Line.name, back_reference: true},
			id: {type: 'string'},
			datasetModelId: {type: 'string'},
			fieldModelId: {type: 'string'},
			textBefore: {type: 'object'},
			textAfter: {type: 'object'},
			visibilityCriteria: {type: 'array', subtype: Entities.VisibilityCriteria.name},
			constraint: {type: Entities.RuleConstraint.name},
			cssCodeForLabel: {type: 'string'},
			cssCodeForInput: {type: 'string'},
			displayLabel: {type: 'boolean'},
			displayPossibleValueLabels: {type: 'boolean'},
			possibleValuesColumnNumber: {type: 'number'},
			possibleValuesColumnWidth: {type: 'number'},
			colspan: {type: 'number'}
		};
	}
	static getFormModelComparator() {
		return (cell_1, cell_2) => {
			if(cell_1.line === cell_2.line) {
				const cells = cell_1.line.cells;
				return cells.indexOf(cell_1) - cells.indexOf(cell_2);
			}
			const line_comparator = Line.getFormModelComparator();
			return line_comparator(cell_1.line, cell_2.line);
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.DATASET,
		RuleEntities.FIELD,
		RuleEntities.FORM
	];

	constructor(values) {
		super();
		this.line = undefined;
		this.id = undefined;
		this.datasetModelId = undefined;
		this.fieldModelId = undefined;
		this.textBefore = {};
		this.textAfter = {};
		this.visibilityCriteria = [];
		this.constraint = undefined;
		this.cssCodeForLabel = undefined;
		this.cssCodeForInput = undefined;
		this.displayLabel = true;
		this.displayPossibleValueLabels = true;
		this.possibleValuesColumnNumber = undefined;
		this.possibleValuesColumnWidth = undefined;
		this.colspan = 1;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	isEmpty() {
		return !this.datasetModelId && !this.fieldModelId && Object.isEmpty(this.textBefore) && Object.isEmpty(this.textAfter);
	}
	hasFieldModel() {
		return !!(this.datasetModelId && this.fieldModelId);
	}
	hasDatasetModel() {
		return !!this.datasetModelId;
	}
	getDatasetModel() {
		if(!this.datasetModelId) {
			throw new Error(`Cell ${this.id} does not refer to any dataset model`);
		}
		return this.line.layout.formModel.study.getDatasetModel(this.datasetModelId);
	}
	getFieldModel() {
		if(!this.fieldModelId) {
			throw new Error(`Cell ${this.id} does not refer to any field model`);
		}
		return this.getDatasetModel().getFieldModel(this.fieldModelId);
	}
	getLocalizedLabel() {
		return this.id;
	}
	getLocalizedTextBefore(languages) {
		return Utils.getLocalizedField.call(this, 'textBefore', languages);
	}
	getLocalizedTextAfter(languages) {
		return Utils.getLocalizedField.call(this, 'textAfter', languages);
	}

	//rulable and layoutable
	getStudy() {
		return this.line.layout.formModel.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.VisibilityCriteria:
				return this.visibilityCriteria.slice();
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.VisibilityCriteria:
				this.visibilityCriteria.push(child);
				child.cell = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}
	getRelations(entity) {
		switch(entity) {
			case Entities.DatasetModel: {
				const dataset_model = this.getDatasetModel();
				return dataset_model ? [dataset_model] : [];
			}
			case Entities.FieldModel: {
				const field_model = this.getFieldModel();
				return field_model ? [field_model] : [];
			}
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}

	//bus
	onChangeDatasetModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.oldValue) {
			this.datasetModelId = event.newValue;
		}
	}
	onDeleteDatasetModel(event) {
		if(this.datasetModelId === event.node.id) {
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
		}
	}

	onChangeFieldModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.node.datasetModel.id && this.fieldModelId && this.fieldModelId === event.oldValue) {
			this.fieldModelId = event.newValue;
		}
	}
	onDeleteFieldModel(event) {
		if(this.datasetModelId === event.node.datasetModel.id && this.fieldModelId === event.node.id) {
			this.datasetModelId = undefined;
			this.fieldModelId = undefined;
		}
	}
	onMoveFieldModel(event) {
		if(this.datasetModelId === event.oldParent.id && this.fieldModelId === event.node.id) {
			this.datasetModelId = event.newParent.id;
		}
	}
	onDeleteVisibilityCriteria(event) {
		this.visibilityCriteria.removeElement(event.node);
	}

	//report
	report(settings) {
		const report = new Report(this);
		Report.checkId(report, this, settings.id_check !== false);
		//check consistency
		if(!this.datasetModelId && this.fieldModelId) {
			report.addError(`Cell ${this.id} refers field model ${this.fieldModelId} but no dataset model`);
		}
		//check validity
		if(this.hasDatasetModel()) {
			try {
				const doc = this.getDatasetModel();
				//cell cannot refers a dataset model multiple if its layout is not repeatable
				if(doc.multiple && !LayoutType[this.line.layout.type].repeatable) {
					report.addError(`Cell ${this.id} refers a multiple dataset model but is not in a repeatable layout`);
				}
				if(!this.fieldModelId) {
					report.addError(`Cell ${this.id} refers only a dataset model`);
				}
			}
			catch(exception) {
				report.addError(exception.message);
			}
		}
		//visibility criteria
		const target_cell_ids = [];
		const target_layout_ids = [];
		this.visibilityCriteria.forEach(function(visibility_criteria) {
			visibility_criteria.targetCellIds.forEach(function(target) {
				if(target_cell_ids.includes(target)) {
					report.addError('Two visibility criteria have the same cell target', visibility_criteria, visibility_criteria['delete'], 'Delete visibility criteria');
				}
				target_cell_ids.push(target);
			});
			visibility_criteria.targetLayoutIds.forEach(function(target) {
				if(target_layout_ids.includes(target)) {
					report.addError('Two visibility criteria have the same layout target', visibility_criteria, visibility_criteria['delete'], 'Delete visibility criteria');
				}
				target_layout_ids.push(target);
			});
		});
		Report.checkLocalizedLabel(report, this, 'textBefore');
		Report.checkLocalizedLabel(report, this, 'textAfter');
		return report;
	}

	//html
	createHTML(languages, name, value) {
		const cell_html = document.createFullElement('div', {id: this.id, style: 'min-height: 25px; position : relative;'});

		if(!Object.isEmpty(this.textBefore)) {
			const cell_text_before = document.createElement('div');
			cell_text_before.innerHTML = this.getLocalizedTextBefore(languages);
			cell_html.appendChild(cell_text_before);
		}
		if(this.datasetModelId) {
			//field model
			if(this.fieldModelId) {
				const field_model = this.getFieldModel();
				const client_id = name || this.id;

				//label
				if(this.displayLabel) {
					const cell_label = document.createFullElement('label', {'for': client_id, style: `float: left; width: 140px;${this.cssCodeForLabel || ''}`}, field_model.getLocalizedShortname(languages));
					if(field_model.getValidators().some(v => v.required)) {
						cell_label.appendChild(document.createFullElement('span', {style: 'margin-left: 0.5rem; color: #EE5F5B;'}, '*'));
					}
					cell_html.appendChild(cell_label);
				}

				let code = '';
				//visibility criteria
				//TODO write a real Javascript function
				this.visibilityCriteria.forEach(function(vc) {

					//build targets array
					code += 'const targets = [];';

					[...vc.targetLayoutIds, ...vc.targetCellIds].forEach(function(target) {
						code += (`targets.push(document.getElementById("${target}"));`);
					}, this);

					//build action
					code += 'for(let i = 0; i < targets.length; i++) {';
					code += 'const target = targets[i];';

					//build condition
					code += 'if (';
					//checkbox
					if(field_model.type === 'CHECKBOX') {
						//there is only two possible values available for checkbox, true or false
						if(vc.values[0] === 'true') {
							code += 'this.checked';
						}
						else {
							code += '!this.checked';
						}
					}
					//checkbox group
					else if(field_model.type === 'CHECKBOX_GROUP') {
						vc.values.forEach(function(value, index) {
							if(index > 0) {
								code += ' || ';
							}
							code += `this.parentNode.querySelector('input[value="${value}"]').checked`;
						});
					}
					//other types
					else {
						vc.values.forEach(function(value, index) {
							if(index > 0) {
								code += ' || ';
							}
							code += `this.value === "${value}"`;
						});
					}
					code += ')';

					if(vc.action === 'SHOW') {
						code += '{if(target.style.display === "none") target.style.display = "";} else if(target.style.display !== "none") {target.style.display = "none";}';
					}
					else {
						code += '{if(target.style.display !== "none") target.style.display = "none";} else if(target.style.display === "none") {target.style.display = "";}';
					}
					code += '}';
				});

				const cell_field = field_model.createHTML(this, languages, client_id, client_id, value ? value.value : '', code);
				//TODO improve following lines
				cell_field.setAttribute('style', (cell_field.getAttribute('style') || '') + (this.cssCodeForInput || ''));
				cell_html.appendChild(cell_field);
			}
			//multiple
			else {
				cell_html.appendChild(this.getDatasetModel().createHTML(languages));
			}
		}
		if(!Object.isEmpty(this.textAfter)) {
			const cell_text_after = document.createElement('div');
			cell_text_after.innerHTML = this.getLocalizedTextAfter(languages);
			cell_html.appendChild(cell_text_after);
		}

		//display or not according to visibility criteria
		const layout_cells = this.line.layout.getCells();
		//cell can be a target only of previous cells
		for(let i = 0; i < layout_cells.indexOf(this); i++) {
			const layout_cell = layout_cells[i];
			for(let j = 0; j < layout_cell.visibilityCriteria.length; j++) {
				const layout_cell_vc = layout_cell.visibilityCriteria[j];
				if(layout_cell_vc.targetCellIds.includes(this.id)) {
					cell_html.style.display = layout_cell_vc.action === 'SHOW' ? 'none' : '';
				}
			}
		}

		return cell_html;
	}
}
