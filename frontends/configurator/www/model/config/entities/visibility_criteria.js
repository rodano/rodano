import '../../../basic-tools/extension.js';
import {FieldModelType} from '../field_model_type.js';
import {Entities} from '../entities.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Report} from '../report.js';

export class VisibilityCriteria extends Node {
	static getProperties() {
		return {
			cell: {type: Entities.Cell.name, back_reference: true},
			operator: {type: 'string'},
			values: {type: 'array'},
			targetCellIds: {type: 'array'},
			targetLayoutIds: {type: 'array'},
			action: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.cell = undefined;
		this.operator = undefined;
		this.values = [];
		this.targetCellIds = [];
		this.targetLayoutIds = [];
		this.action = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return 'Visibility criterion';
	}

	//bus
	onChangeCellId(event) {
		if(this.cell.line.layout.formModel === event.node.line.layout.formModel) {
			this.targetCellIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteCell(event) {
		if(this.cell.line.layout.formModel === event.node.line.layout.formModel) {
			this.targetCellIds.removeElement(event.node.id);
			if(this.targetCellIds.isEmpty() && this.targetLayoutIds.isEmpty()) {
				this['delete']();
			}
		}
	}

	onChangeLayoutId(event) {
		if(this.cell.line.layout.formModel === event.node.formModel) {
			this.targetLayoutIds.replace(event.oldValue, event.newValue);
		}
	}
	onDeleteLayout(event) {
		if(this.cell.line.layout.formModel === event.node.formModel) {
			this.targetLayoutIds.removeElement(event.node.id);
			if(this.targetCellIds.isEmpty() && this.targetLayoutIds.isEmpty()) {
				this['delete']();
			}
		}
	}

	//report
	report() {
		const report = new Report(this);
		if(this.values.isEmpty()) {
			report.addError('Visibility criteria does not have any value', this, this['delete'], 'Delete visibility criteria');
		}
		if(this.targetCellIds.isEmpty() && this.targetLayoutIds.isEmpty()) {
			report.addError('Visibility criteria does not have any cell or layout target', this, this['delete'], 'Delete visibility criteria');
		}
		//check values
		const field_model = this.cell.getFieldModel();
		const field_model_type = FieldModelType[field_model.type];
		//check values
		if(field_model_type.is_multiple_choice || field_model_type === FieldModelType.CHECKBOX) {
			const possible_value_ids = field_model.type === 'CHECKBOX' ? ['true', 'false'] : field_model.possibleValues.map(pv => pv.id);
			this.values.forEach(value => {
				if(!possible_value_ids.includes(value)) {
					report.addError(
						`Visibility criteria on cell ${this.cell.id} contains value ${value} which is not a possible value of field model ${field_model.id}`,
						this,
						function() {
							this.values.removeElement(value);
							//delete visibility criteria if there is no value left
							if(this.values.isEmpty()) {
								this['delete']();
							}
						},
						'Delete value'
					);
				}
			});
		}
		//check targets
		const layout = this.cell.line.layout;
		const available_layout_ids = layout.formModel.layouts.map(l => l.id);
		this.targetLayoutIds.forEach(function(target) {
			if(!available_layout_ids.includes(target)) {
				report.addError(
					`Visibility criteria on cell ${this.cell.id} contains layout target ${target} which does not match any id of a layout on the same form model`,
					this,
					function() {
						this.targetLayoutIds.removeElement(target);
						//delete visibility criteria if there is no target left
						if(this.targetCellIds.isEmpty() && this.targetLayoutIds.isEmpty()) {
							this['delete']();
						}
					},
					'Remove layout target'
				);
			}
		}, this);
		const available_cell_ids = layout.lines.flatMap(l => l.cells).map(c => c.id);
		this.targetCellIds.forEach(function(target) {
			if(!available_cell_ids.includes(target)) {
				report.addError(
					`Visibility criteria on cell ${this.cell.id} contains cell target ${target} which does not match any id of a cell on the same layout`,
					this,
					function() {
						this.targetCellIds.removeElement(target);
						//delete visibility criteria if there is no target left
						if(this.targetCellIds.isEmpty() && this.targetLayoutIds.isEmpty()) {
							this['delete']();
						}
					},
					'Remove cell target'
				);
			}
		}, this);
		return report;
	}
}
