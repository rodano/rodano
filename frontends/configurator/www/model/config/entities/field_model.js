import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {Study} from './study.js';
import {ComparatorUtils} from '../comparator_utils.js';
import {Report} from '../report.js';
import {DisplayableNode} from '../node_displayable.js';
import {FieldModelType} from '../field_model_type.js';
import {DataType} from '../data_type.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

export class FieldModel extends DisplayableNode {
	static getProperties() {
		return {
			datasetModel: {type: Entities.DatasetModel.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			type: {type: 'string'},
			dataType: {type: 'string'},
			plugin: {type: 'boolean'},
			valueConstraint: {type: Entities.RuleConstraint.name},
			valueFormula: {type: 'string'},
			possibleValues: {type: 'array', subtype: Entities.PossibleValue.name},
			dictionary: {type: 'string'},
			possibleValuesProvider: {type: 'string'},
			possibleValuesProviderDescription: {type: 'string'},
			validatorIds: {type: 'array'},
			workflowIds: {type: 'array'},
			rules: {type: 'array', subtype: Entities.Rule.name},
			searchable: {type: 'boolean'},
			readOnly: {type: 'boolean'},
			exportable: {type: 'boolean'},
			allowDateInFuture: {type: 'boolean'},
			exportOrder: {type: 'number'},
			exportLabel: {type: 'string'},
			maxLength: {type: 'number'},
			maxIntegerDigits: {type: 'number'},
			maxDecimalDigits: {type: 'number'},
			minValue: {type: 'number'},
			maxValue: {type: 'number'},
			minYear: {type: 'number'},
			maxYear: {type: 'number'},
			matcher: {type: 'string'},
			matcherMessage: {type: 'object'},
			withYears: {type: 'boolean'},
			withMonths: {type: 'boolean'},
			withDays: {type: 'boolean'},
			withHours: {type: 'boolean'},
			withMinutes: {type: 'boolean'},
			withSeconds: {type: 'boolean'},
			yearsMandatory: {type: 'boolean'},
			monthsMandatory: {type: 'boolean'},
			daysMandatory: {type: 'boolean'},
			hoursMandatory: {type: 'boolean'},
			minutesMandatory: {type: 'boolean'},
			secondsMandatory: {type: 'boolean'},
			inlineHelp: {type: 'string'},
			advancedHelp: {type: 'object'},
		};
	}
	static getExportComparator() {
		return (a1, a2) => ComparatorUtils.compareFields(a1, a2, ['exportOrder', 'id']);
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.DATASET,
		RuleEntities.FIELD
	];

	constructor(values) {
		super();
		this.datasetModel = undefined,
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.type = 'STRING';
		this.dataType = 'STRING';
		this.plugin = false;
		this.valueConstraint = undefined;
		this.valueFormula = undefined;
		this.possibleValues = [];
		this.dictionary = undefined;
		this.possibleValuesProvider = undefined;
		this.possibleValuesProviderDescription = undefined;
		this.validatorIds = [];
		this.workflowIds = [];
		this.rules = [];
		this.searchable = false;
		this.readOnly = false;
		this.exportable = true;
		this.allowDateInFuture = false;
		this.exportOrder = undefined;
		this.exportLabel = undefined;
		this.maxLength = undefined;
		this.maxIntegerDigits = undefined;
		this.maxDecimalDigits = undefined;
		this.minValue = undefined;
		this.maxValue = undefined;
		this.minYear = undefined;
		this.maxYear = undefined;
		this.matcher = undefined;
		this.matcherMessage = {};
		this.withYears = false;
		this.withMonths = false;
		this.withDays = false;
		this.withHours = false;
		this.withMinutes = false;
		this.withSeconds = false;
		this.yearsMandatory = true;
		this.monthsMandatory = true;
		this.daysMandatory = true;
		this.hoursMandatory = true;
		this.minutesMandatory = true;
		this.secondsMandatory = true;
		this.inlineHelp = undefined;
		this.advancedHelp = {};
		EntitiesHooks?.CreateNode.call(this, values);
	}

	isNumber() {
		if(this.type === 'NUMBER') {
			return true;
		}
		if(FieldModelType[this.type].is_multiple_choice) {
			return this.possibleValues.map(pv => pv.id).every(Number.isNumber);
		}
		return false;
	}
	isRangeValidable() {
		return this.type === 'DATE' || this.isNumber();
	}
	getFormModels() {
		const field_model_id = this.id;
		const dataset_model_id = this.datasetModel.id;
		function field_model_cell(cell) {
			return cell.datasetModelId === dataset_model_id && cell.fieldModelId === field_model_id;
		}
		function field_model_line(line) {
			return line.cells.some(field_model_cell);
		}
		function field_model_layout(layout) {
			return layout.lines.some(field_model_line);
		}
		//logical method
		/*return this.datasetModel.study.formModels.filter(function(form_model) {
			return form_model.getCells().some(field_model_cell);
		});*/
		//optimized method
		return this.datasetModel.study.formModels.filter(function(form_model) {
			return form_model.layouts.some(field_model_layout);
		});
	}
	getWorkflows() {
		return this.workflowIds.map(Study.prototype.getWorkflow, this.datasetModel.study);
	}
	getValidators() {
		return this.validatorIds.map(Study.prototype.getValidator, this.datasetModel.study);
	}
	getPossibleValue(possible_value_id) {
		return Utils.getObjectById(this.possibleValues, possible_value_id);
	}
	getMaxLength() {
		if(DataType.STRING.name === this.dataType && this.maxLength) {
			return this.maxLength;
		}
		return FieldModelType[this.type].default_max_length;
	}
	generateCellId(form_model) {
		let cell_id = this.id;
		//prepare concatenation of dataset model id of there is enough room
		if(cell_id.length < DisplayableNode.ID_MAX_LENGTH - 1) {
			cell_id = `_${cell_id}`;
			//concatenate full dataset model id
			if(cell_id.length + this.datasetModel.id.length < DisplayableNode.ID_MAX_LENGTH) {
				cell_id = this.datasetModel.id + cell_id;
			}
			else {
				//concatenate shortened dataset model id (id with only initials)
				const shortened_dataset_model_id = Utils.shortenId(this.datasetModel.id);
				if(cell_id.length + shortened_dataset_model_id.length < DisplayableNode.ID_MAX_LENGTH) {
					cell_id = shortened_dataset_model_id + cell_id;
				}
				else {
					//concatenate first letter of dataset model id
					cell_id = this.datasetModel.id[0] + cell_id;
				}
			}
		}
		//check that id does not already exist in the form model
		const cell_ids = form_model.getCells().map(c => c.id);
		let index = 0;
		let unique_cell_id = cell_id;
		while(cell_ids.includes(unique_cell_id)) {
			const index_string = `${index}`;
			unique_cell_id = `${cell_id.slice(0, - (index_string.length + 1))}_${index_string}`;
			index++;
		}
		return unique_cell_id;
	}

	//rulable and layoutable
	getStudy() {
		return this.datasetModel.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.PossibleValue:
				return this.possibleValues.slice();
			case Entities.Rule:
				return this.rules.slice();
			case Entities.RuleConstraint:
				return this.valueConstraint ? [this.valueConstraint] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.PossibleValue:
				this.possibleValues.push(child);
				child.fieldModel = this;
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
			case Entities.FormModel:
				return this.getFormModels();
			case Entities.Workflow:
				return this.getWorkflows();
			case Entities.TimelineGraphSection: {
				const timeline_graph_sections = [].concat(...this.datasetModel.study.timelineGraphs.map(g => g.sections));
				return timeline_graph_sections
					.filter(s => s.datasetModelId && s.datasetModelId === this.datasetModel.id)
					.filter(s => s.dateFieldModelId === this.id || s.endDateFieldModelId === this.id || s.valueFieldModelId === this.id || s.labelFieldModelId === this.id || s.metaFieldModelIds?.includes(this.id));
			}
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}
	//entity field model is an exception because it is not possible to know if it is used based on the configuration
	//TODO in theory, "isUsed" should be consistent with "getUsage", this is not the case with field models
	isUsed () {
		return this.plugin || super.isUsed();
	}

	//bus
	onDeletePossibleValue(event) {
		this.possibleValues.removeElement(event.node);
	}
	onChangeWorkflowId(event) {
		this.workflowIds.replace(event.oldValue, event.newValue);
	}
	onDeleteWorkflow(event) {
		this.workflowIds.removeElement(event.node.id);
	}
	onChangeValidatorId(event) {
		this.validatorIds.replace(event.oldValue, event.newValue);
	}
	onDeleteValidator(event) {
		this.validatorIds.removeElement(event.node.id);
	}
	onDeleteRule(event) {
		this.rules.removeElement(event.node);
	}

	/*getUsage() {
		return this.plugin ? this.datasetModel.getUsage() : super.getUsage();
	}*/

	validateValue(value) {
		if(this.type === 'NUMBER') {
			if(value.includes('.')) {
				const parts = value.split('.');
				if(parts[0].length > this.maxIntegerDigits) {
					return false;
				}
				if(parts[1].length > this.maxDecimalDigits) {
					return false;
				}
			}
			else {
				if(value.length > this.maxIntegerDigits) {
					return false;
				}
			}
			const number = parseFloat(value);
			if(this.minValue && number < this.minValue) {
				return false;
			}
			if(this.maxValue && number > this.maxValue) {
				return false;
			}
		}
		if(this.type === 'DATE') {
			let regexp = '';
			if(this.withDays) {
				regexp += '\\d{2}.';
			}
			if(this.withMonths) {
				regexp += '\\d{2}.';
			}
			if(this.withYears) {
				regexp += '\\d{4}';
			}
			if(regexp && (this.withHours || this.withMinutes || this.withSeconds)) {
				regexp += ' ';
			}
			if(this.withHours) {
				regexp += '\\d{2}';
			}
			if(this.withMinutes) {
				regexp += ':\\d{2}';
			}
			if(this.withSeconds) {
				regexp += ':\\d{2}';
			}
			return value.match(new RegExp(`^${regexp}$`));
		}
		return true;
	}

	//report
	report(settings) {
		const report = super.report(settings);
		//unused
		if(!this.isUsed()) {
			report.addInfo(`Field model ${this.id} in dataset model ${this.datasetModel.id} is unused`, this, this['delete'], 'Delete field model');
		}
		//value formula
		if(this.valueFormula && !this.valueFormula.startsWith('=')) {
			if(FieldModelType[this.type].is_multiple_choice && !this.possibleValues.map(pv => pv.id).includes(this.valueFormula)) {
				report.addError(`Field model ${this.id} has ${this.valueFormula} as a default value but this is not a possible value of this field model`, this);
			}
			if(!this.validateValue(this.valueFormula)) {
				report.addError(`Field model ${this.id} has ${this.valueFormula} as a default value but this is not compliant with field model restrictions`, this);
			}
		}
		//export
		if(this.exportable && this.exportOrder === undefined) {
			report.addWarning(`Field model ${this.id} is exportable but does not have an export order`);
		}
		//possible values
		if(FieldModelType[this.type].is_multiple_choice) {
			this.possibleValues.forEach(function(possible_value) {
				if(possible_value.specify && this.type !== 'CHECKBOX_GROUP') {
					report.addError(
						`Field model ${this.id} contains possible value ${possible_value.id} which has "specify" flag whereas this flag can only be used for advanced selects or checkbox groups`,
						this,
						function() {
							possible_value.specify = false;
						},
						'Remove flag "specify" for this possible value'
					);
				}
				//possible value must comply with more rules for field model which can have multiple values
				if(FieldModelType[this.type].has_multiple_values) {
					//possible value id must comply with global id policy
					Report.checkId(report, possible_value, settings.id_check !== false);
				}
			}, this);
		}
		//validators and workflows
		this.getValidators().forEach(function(validator) {
			if(validator.workflowId && !this.workflowIds.includes(validator.workflowId)) {
				report.addError(
					`Field model ${this.id} is validated against validator ${validator.id} which generate workflow ${validator.workflowId} whereas this workflow is not available for it`,
					this,
					function() {
						this.workflowIds.push(validator.workflowId);
					},
					`Add workflow ${validator.workflowId} to field model`
				);
			}
		}, this);
		return report;
	}

	//html
	createHTML(cell, languages, id, name, value, onchange) {
		const that = this;
		const field_name = name || `${this.datasetModel.id}_${this.id}`;
		const field_value = value || '';
		let field;

		function get_width() {
			if(that.size) {
				return `${that.size + 2}rem`;
			}
			if(that.type === FieldModelType.NUMBER.name) {
				let digits = that.maxIntegerDigits;
				if(that.maxDecimalDigits) {
					digits += (that.maxDecimalDigits + 1);
				}
				return `${digits + 2}rem`;
			}
			return 'auto';
		}
		const width = get_width();

		//radio
		if(this.type === FieldModelType.RADIO.name) {
			field = document.createFullElement('div', {style: 'float: left;'});
			for(let i = 0; i < this.possibleValues.length; i++) {
				const possible_value = this.possibleValues[i];
				//container
				const container_width = cell.possibleValuesColumnWidth ? `${cell.possibleValuesColumnWidth}px` : 'auto';
				const container = document.createFullElement('span', {style: `display: inline-block; width: ${container_width};`});
				if(!cell.possibleValuesColumnWidth) {
					container.style.marginRight = '1.5rem';
				}
				field.appendChild(container);
				//input
				const radio = document.createElement('input');
				radio.setAttribute('type', 'radio');
				radio.setAttribute('name', field_name);
				radio.setAttribute('value', possible_value.id);
				radio.setAttribute('style', 'margin: 0 0.5rem 0 0; vertical-align: middle;');
				if(onchange) {
					radio.setAttribute('onchange', onchange);
				}
				if(this.readOnly) {
					radio.setAttribute('disabled', 'disabled');
				}
				if(field_value === possible_value.id) {
					radio.setAttribute('checked', 'checked');
				}
				field.appendChild(container);
				//label
				if(cell.displayPossibleValueLabels) {
					const radio_label = document.createFullElement('label', {style: 'display: inline; float: none;'});
					radio_label.appendChild(radio);
					radio_label.appendChild(document.createTextNode(possible_value.getLocalizedShortname(languages)));
					container.appendChild(radio_label);
				}
				else {
					container.appendChild(radio);
				}
				//break line
				if((i + 1) % cell.possibleValuesColumnNumber === 0) {
					field.appendChild(document.createElement('br'));
				}
			}
		}
		//checkbox group
		else if(this.type === FieldModelType.CHECKBOX_GROUP.name) {
			field = document.createFullElement('div', {style: 'float: left;'});
			const specify_possible_value = this.possibleValues.find(p => p.specify);
			//updater
			const update_full_field = function() {
				const values = checkboxes.filter(c => c.checked).map(c => c.value);
				//display other input if specify value has been checked
				if(specify_possible_value) {
					if(values.includes(specify_possible_value.id)) {
						checkbox_other.style.display = 'inline';
						if(checkbox_other?.value) {
							//remove commas because comma is the delimiter
							values.push(checkbox_other.value.replace(/,/g, ' '));
						}
					}
					else {
						checkbox_other.style.display = 'none';
					}
					values.removeElement(specify_possible_value.id);
				}
				full_field.value = values.join(',');
				const change = new UIEvent('change', {bubbles: true, cancelable: true});
				full_field.dispatchEvent(change);
			};
			const field_values = field_value ? field_value.split(',') : [];
			const checkboxes = [];
			let checkbox_other;
			for(let i = 0; i < this.possibleValues.length; i++) {
				const possible_value = this.possibleValues[i];
				//container
				const container_width = cell.possibleValuesColumnWidth ? `${cell.possibleValuesColumnWidth}px` : 'auto';
				const container = document.createFullElement('span', {style: `display: inline-block; width: ${container_width};`});
				if(!cell.possibleValuesColumnWidth) {
					container.style.marginRight = '1.5rem';
				}
				field.appendChild(container);
				//checkbox
				const checkbox = document.createElement('input');
				checkbox.setAttribute('name', `${field_name}_${possible_value.id}`);
				checkbox.setAttribute('type', 'checkbox');
				//checkbox.setAttribute('name', field_name);
				checkbox.setAttribute('value', possible_value.id);
				checkbox.setAttribute('style', 'margin: 0 0.5rem 0 0; vertical-align: middle;');
				if(this.readOnly) {
					checkbox.setAttribute('disabled', 'disabled');
				}
				if(field_values.includes(possible_value.id)) {
					field_values.removeElement(possible_value.id);
					checkbox.setAttribute('checked', 'checked');
				}
				checkbox.onchange = update_full_field;
				checkboxes.push(checkbox);
				//label
				if(cell.displayPossibleValueLabels) {
					const checkbox_label = document.createFullElement('label', {style: 'display: inline; float: none;'});
					checkbox_label.appendChild(checkbox);
					checkbox_label.appendChild(document.createTextNode(possible_value.getLocalizedShortname(languages)));
					container.appendChild(checkbox_label);
				}
				else {
					container.appendChild(checkbox);
				}
				//break line
				if((i + 1) % cell.possibleValuesColumnNumber === 0) {
					field.appendChild(document.createElement('br'));
				}
			}
			if(specify_possible_value) {
				//input
				checkbox_other = document.createElement('input');
				checkbox_other.setAttribute('type', 'text');
				checkbox_other.setAttribute('style', 'display: none; width: 140px;');
				if(this.readOnly) {
					checkbox_other.setAttribute('disabled', 'disabled');
				}
				if(field_values.length > 0) {
					checkbox_other.setAttribute('value', field_values[0]);
				}
				checkbox_other.onchange = update_full_field;
				field.appendChild(checkbox_other);
			}
			const full_field = document.createElement('input');
			full_field.setAttribute('type', 'hidden');
			full_field.setAttribute('name', field_name);
			full_field.setAttribute('value', field_value || '');
			if(onchange) {
				full_field.setAttribute('onchange', onchange);
			}
			field.appendChild(full_field);
		}
		//date select
		else if(this.type === FieldModelType.DATE_SELECT.name) {
			field = document.createElement('div');
			field.setAttribute('style', 'float: left');
			//fields
			let days, months, years;
			//updater
			const that = this;
			const update_full_field = function() {
				full_field.value = '';
				if(days?.value || months?.value || years?.value) {
					if(that.withDays) {
						full_field.value += days.value;
					}
					if(that.withMonths) {
						if(that.withDays) {
							full_field.value += '.';
						}
						full_field.value += months.value;
					}
					if(that.withYears) {
						if(that.withMonths) {
							full_field.value += '.';
						}
						full_field.value += years.value;
					}
				}
			};
			const field_values = field_value ? field_value.split('.') : [];
			//days
			if(this.withDays) {
				days = document.createElement('select');
				days.setAttribute('name', `${field_name}_days`);
				if(this.readOnly) {
					days.setAttribute('disabled', 'disabled');
				}
				days.style.width = '45px';
				const days_value = field_values.first();
				//blank option
				const days_blank_option = document.createElement('option');
				days_blank_option.setAttribute('value', '');
				if(!days_value) {
					days_blank_option.setAttribute('selected', 'selected');
				}
				days_blank_option.appendChild(document.createTextNode('--'));
				days.appendChild(days_blank_option);
				//other options
				for(let i = 1; i < 32; i++) {
					const days_option = document.createElement('option');
					days_option.setAttribute('value', i.pad(2));
					if(days_value === i.pad(2)) {
						days_option.setAttribute('selected', 'selected');
					}
					days_option.appendChild(document.createTextNode(i.pad(2)));
					days.appendChild(days_option);
				}
				//unknown option
				if(!this.daysMandatory) {
					const days_unknown = document.createElement('option');
					days_unknown.setAttribute('value', 'Unknown');
					if(days_value === 'Unknown') {
						days_unknown.setAttribute('selected', 'selected');
					}
					days_unknown.appendChild(document.createTextNode('Unknown'));
					days.appendChild(days_unknown);
				}
				field.appendChild(days);
				days.onchange = update_full_field;
			}
			//months
			if(this.withMonths) {
				months = document.createElement('select');
				months.setAttribute('name', `${field_name}_months`);
				if(this.readOnly) {
					months.setAttribute('disabled', 'disabled');
				}
				months.style.width = '55px';
				let months_value;
				if(this.withDays) {
					months_value = field_values[1];
					months.style.marginLeft = '5px';
				}
				else {
					months_value = field_values[0];
				}
				//blank option
				const months_blank_option = document.createElement('option');
				months_blank_option.setAttribute('value', '');
				if(!months_value) {
					months_blank_option.setAttribute('selected', 'selected');
				}
				months_blank_option.appendChild(document.createTextNode('--'));
				months.appendChild(months_blank_option);
				//other options
				const months_options = ['Jan.', 'Feb.', 'Mar.', 'Apr.', 'May.', 'June', 'July', 'Aug.', 'Sep.', 'Oct.', 'Nov.', 'Dec.'];
				for(let i = 0; i < months_options.length; i++) {
					const months_option = document.createElement('option');
					months_option.setAttribute('value', (i + 1).pad(2));
					if(months_value === (i + 1).pad(2)) {
						months_option.setAttribute('selected', 'selected');
					}
					months_option.appendChild(document.createTextNode(months_options[i]));
					months.appendChild(months_option);
				}
				//unknown option
				if(!this.monthsMandatory) {
					const months_unknown = document.createElement('option');
					months_unknown.setAttribute('value', 'Unknown');
					if(months_value === 'Unknown') {
						months_unknown.setAttribute('selected', 'selected');
					}
					months_unknown.appendChild(document.createTextNode('Unknown'));
					months.appendChild(months_unknown);
				}
				field.appendChild(months);
				months.onchange = update_full_field;
			}
			//years
			if(this.withYears) {
				years = document.createElement('select');
				years.setAttribute('name', `${field_name}_years`);
				if(this.readOnly) {
					years.setAttribute('disabled', 'disabled');
				}
				years.style.width = '65px';
				const years_value = field_values.last();
				if(field.children.length > 0) {
					years.style.marginLeft = '5px';
				}
				//blank option
				const years_blank_option = document.createElement('option');
				years_blank_option.setAttribute('value', '');
				if(!years_value) {
					years_blank_option.setAttribute('selected', 'selected');
				}
				years_blank_option.appendChild(document.createTextNode('--'));
				years.appendChild(years_blank_option);
				//other options
				for(let i = (this.yearsStop || new Date().getFullYear()); i >= this.yearsStart; i--) {
					const years_option = document.createElement('option');
					years_option.setAttribute('value', i);
					if(parseInt(years_value) === i) {
						years_option.setAttribute('selected', 'selected');
					}
					years_option.appendChild(document.createTextNode(i));
					years.appendChild(years_option);
				}
				//unknown option
				if(!this.yearsMandatory) {
					const years_unknown = document.createElement('option');
					years_unknown.setAttribute('value', 'Unknown');
					if(years_value === 'Unknown') {
						years_unknown.setAttribute('selected', 'selected');
					}
					years_unknown.appendChild(document.createTextNode('Unknown'));
					years.appendChild(years_unknown);
				}
				field.appendChild(years);
				years.onchange = update_full_field;
			}
			//set id on first select
			if(id) {
				//field may not have any select if neither "display days" nor "display months" nor "display years" have been ticked
				//in this case the field model is malformed
				field.querySelector('select')?.setAttribute('id', id);
			}
			//rebuilt field
			const full_field = document.createElement('input');
			full_field.setAttribute('type', 'hidden');
			full_field.setAttribute('name', field_name);
			full_field.setAttribute('value', field_value || '');
			field.appendChild(full_field);
		}
		//textarea
		else if(this.type === FieldModelType.TEXTAREA.name) {
			field = document.createElement('textarea');
			field.setAttribute('name', field_name);
			if(id) {
				field.setAttribute('id', id);
			}
			if(this.readOnly) {
				field.setAttribute('disabled', 'disabled');
			}
			field.appendChild(document.createTextNode(field_value));
		}
		//select
		else if(this.type === FieldModelType.SELECT.name) {
			field = document.createElement('select');
			field.setAttribute('style', `width: ${width}; max-width: 15rem;`);
			const blank_option = document.createElement('option');
			blank_option.setAttribute('value', '');
			if(!field_value) {
				blank_option.setAttribute('selected', 'selected');
			}
			blank_option.appendChild(document.createTextNode('--'));
			field.appendChild(blank_option);
			for(let i = 0; i < this.possibleValues.length; i++) {
				const possible_value = this.possibleValues[i];
				const option = document.createElement('option');
				option.setAttribute('value', possible_value.id);
				if(field_value === possible_value.id) {
					option.setAttribute('selected', 'selected');
				}
				option.appendChild(document.createTextNode(possible_value.getLocalizedShortname(languages)));
				field.appendChild(option);
			}
			if(onchange) {
				field.setAttribute('onchange', onchange);
			}
			field.setAttribute('name', field_name);
			field.setAttribute('value', field_value);
			if(id) {
				field.setAttribute('id', id);
			}
			if(this.readOnly) {
				field.setAttribute('disabled', 'disabled');
			}
		}
		//input
		else {
			field = document.createElement('div');
			const input = document.createElement('input');
			input.setAttribute('style', `width: ${width};`);
			switch(this.type) {
				case 'DATE':
					//input.setAttribute('type', 'date');
					input.setAttribute('type', 'text');
					input.setAttribute('style', `${input.getAttribute('style')} max-width: 15rem;`);
					input.setAttribute('value', field_value);
					break;
				case 'NUMBER':
					//input.setAttribute('type', 'number');
					input.setAttribute('type', 'text');
					input.setAttribute('style', `${input.getAttribute('style')} text-align: right;`);
					input.setAttribute('value', field_value);
					break;
				case 'CHECKBOX':
					input.setAttribute('type', 'checkbox');
					if(field_value === 'true') {
						input.setAttribute('checked', 'checked');
					}
					if(onchange) {
						input.setAttribute('onchange', onchange);
					}
					break;
				default:
					input.setAttribute('type', 'text');
					input.setAttribute('value', field_value);
			}
			input.setAttribute('name', field_name);
			if(id) {
				input.setAttribute('id', id);
			}
			if(this.readOnly) {
				input.setAttribute('disabled', 'disabled');
			}
			field.appendChild(input);
			if(this.inlineHelp) {
				input.style.marginRight = '5px';
				field.appendChild(document.createTextNode(this.inlineHelp));
			}
		}
		return field;
	}
	createFullHTML(cell, languages, id) {
		const container = document.createElement('div');
		//build field
		const field = this.createHTML(cell, languages, id);
		field.setAttribute('style', 'float: left;');
		//build label
		const label = document.createFullElement('label', {}, this.getLocalizedShortname(languages));
		if(this.getValidators().some(v => v.required)) {
			label.appendChild(document.createFullElement('span', {style: 'margin-left: 0.5rem; color: #EE5F5B;'}, '*'));
		}
		//link label and form field
		if(id && this.type !== 'RADIO') {
			label.setAttribute('for', id);
		}
		container.appendChild(label);
		container.appendChild(field);
		return container;
	}
}
