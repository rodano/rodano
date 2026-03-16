import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';
import {Report} from '../report.js';
import {LayoutType} from '../layout_type.js';
import {Entities} from '../entities.js';
import {RuleEntities} from '../rule_entities.js';

export class Layout extends Node {
	static getProperties() {
		return {
			formModel: {type: Entities.FormModel.name, back_reference: true},
			id: {type: 'string'},
			description: {type: 'object'},
			type: {type: 'string'},
			datasetModelId: {type: 'string'},
			defaultSortFieldModelId: {type: 'string'},
			textBefore: {type: 'object'},
			textAfter: {type: 'object'},
			columns: {type: 'array', subtype: Entities.Column.name},
			lines: {type: 'array', subtype: Entities.Line.name},
			constraint: {type: Entities.RuleConstraint.name},
			cssCode: {type: 'string'}
		};
	}
	static getFormModelComparator() {
		return (layout_1, layout_2) => {
			if(layout_1.formModel === layout_2.formModel) {
				const layouts = layout_1.formModel.layouts;
				return layouts.indexOf(layout_1) - layouts.indexOf(layout_2);
			}
			return layout_1.formModel.id.compareTo(layout_2.formModel.id);
		};
	}
	static RuleEntities = [
		RuleEntities.SCOPE,
		RuleEntities.EVENT,
		RuleEntities.FORM
	];

	constructor(values) {
		super();
		this.formModel = undefined;
		this.id = undefined;
		this.description = {};
		this.type = 'SINGLE';
		this.datasetModelId = undefined;
		this.defaultSortFieldModelId = undefined;
		this.textBefore = {};
		this.textAfter = {};
		this.columns = [];
		this.lines = [];
		this.constraint = undefined;
		this.cssCode = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getDatasetModel() {
		return this.datasetModelId ? this.formModel.study.getDatasetModel(this.datasetModelId) : undefined;
	}
	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'description', languages) || this.id;
	}
	getLocalizedTextBefore(languages) {
		return Utils.getLocalizedField.call(this, 'textBefore', languages);
	}
	getLocalizedTextAfter(languages) {
		return Utils.getLocalizedField.call(this, 'textAfter', languages);
	}
	getCells() {
		return this.lines.flatMap(l => l.cells);
	}

	//rulable and layoutable
	getStudy() {
		return this.formModel.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.Line:
				return this.lines.slice();
			case Entities.Column:
				return this.columns.slice();
			case Entities.RuleConstraint:
				return this.constraint ? [this.constraint] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.Line:
				this.lines.push(child);
				child.layout = this;
				break;
			case Entities.Column:
				this.columns.push(child);
				child.layout = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onDeleteColumn(event) {
		const index = this.columns.indexOf(event.node);
		if(index !== -1) {
			this.columns.remove(index);
			this.lines.forEach(line => {
				let offset = 0;
				let i;
				//find cell for deleted column
				for(i = 0; i <= index; i++) {
					offset += (line.cells[i].colspan || 1);
					//index starts at 0 whereas offset starts at 1
					if(offset - 1 >= index) {
						break;
					}
				}
				const cell = line.cells[i];
				//cell at index is a real single cell
				if(i === index && (!cell.colspan || cell.colspan === 1)) {
					line.cells.remove(i);
				}
				//there is no cell at index or cell at index has a colspan
				else {
					cell.colspan--;
				}
			});
		}
	}

	onChangeDatasetModelId(event) {
		if(this.datasetModelId && this.datasetModelId === event.oldValue) {
			this.datasetModelId = event.newValue;
		}
	}
	onDeleteDatasetModel(event) {
		if(this.datasetModelId && this.datasetModelId === event.node.id) {
			this.delete();
		}
	}

	//report
	report(settings) {
		const report = new Report(this);
		Report.checkId(report, this, settings.id_check !== false);
		//check multiple
		if(LayoutType[this.type].repeatable) {
			if(!this.datasetModelId) {
				report.addError(`Layout ${this.id} is of type ${this.type}, which is repeatable, but does not refer any dataset model`);
			}
			else if(!this.getDatasetModel().multiple) {
				report.addError(`Layout ${this.id} is of type ${this.type}, which is repeatable, refers a dataset model that is not multiple`);
			}
		}
		for(let i = this.lines.length - 1; i >= 0; i--) {
			const line = this.lines[i];
			let columns_number = 0;
			for(let j = line.cells.length - 1; j >= 0; j--) {
				columns_number += line.cells[j].colspan;
			}
			if(columns_number !== this.columns.length) {
				report.addError(`Layout ${this.id} contains ${this.columns.length} columns but contains ${columns_number} cells on line number ${i}`);
			}
		}
		//check uniqueness of cell id in addition to the check of uniqueness of cell id in form model in order to check conditional layouts
		const cells = this.getCells();
		const cell_ids = [];
		for(let i = cells.length - 1; i >= 0; i--) {
			const cell = cells[i];
			if(cell_ids.includes(cell.id)) {
				report.addError(
					`Layout ${this.id} contains at least two cells with id ${cell.id}`,
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
		}
		Report.checkLocalizedLabel(report, this, 'textBefore');
		Report.checkLocalizedLabel(report, this, 'textAfter');
		return report;
	}

	//html
	createHTML(languages, datasets, objects) {
		const that = this;

		//find interesting field models for this dataset, used only by multiple layouts
		const meaningful_field_models = this.getDatasetModel()?.getMeaningfulFieldModels() ?? [];

		function draw_single_layout() {
			//create table
			const layout_table = document.createFullElement('table', {id: that.id, style: 'width: 100%;'});
			//create columns
			const layout_head = document.createElement('thead');
			const columns = document.createElement('tr');
			layout_head.appendChild(columns);
			for(let i = 0, length = that.columns.length; i < length; i++) {
				const column = that.columns[i];
				columns.appendChild(document.createFullElement('th', {style: column.cssCode || ''}));
			}
			layout_table.appendChild(layout_head);
			//create lines
			const layout_body = document.createElement('tbody');
			for(let i = 0, length = that.lines.length; i < length; i++) {
				layout_body.appendChild(that.lines[i].createHTML(languages, datasets));
			}
			layout_table.appendChild(layout_body);
			return layout_table;
		}

		function draw_lines(dataset) {
			const layout_body_line = document.createElement('tr');
			layout_body_line.data = dataset;

			//add one cell to expand/collapse line
			const layout_line_cell_control = document.createFullElement('td', {style: 'padding: 0.5rem; border: 1px solid #ccc;'});
			const layout_line_cell_control_toggle = document.createFullElement('img', {src: 'images/bullet_arrow_right.png', alt: 'Expand', title: 'Expand', style: 'cursor: pointer;'});
			layout_line_cell_control_toggle.addEventListener(
				'click',
				function(event) {
					event.stop();
					layout_expanded.style.display = layout_expanded.style.display === 'none' ? '' : 'none';
					this.setAttribute('src', layout_expanded.style.display === 'none' ? 'images/bullet_arrow_right.png' : 'images/bullet_arrow_down.png');
				}
			);
			layout_line_cell_control.appendChild(layout_line_cell_control_toggle);
			layout_body_line.appendChild(layout_line_cell_control);

			//add one cell per meaningful field model
			meaningful_field_models.forEach(function(field_model) {
				const cell_name = `${dataset.datasetModelId}:${dataset.number}:${field_model.id}`;
				const layout_body_cell = document.createFullElement('td', {id: cell_name, style: 'padding: 0.5rem; border: 1px solid #ccc;'});
				layout_body_cell.appendChild(field_model.createHTML(undefined, languages, cell_name, dataset.getField(field_model.id).value));
				layout_body_line.appendChild(layout_body_cell);
			});

			//add one column for actions
			const layout_body_cell_actions = document.createFullElement('td', {style: 'padding: 0.5rem; border: 1px solid #ccc;'});
			const layout_body_cell_actions_delete = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', title: 'Delete', style: 'cursor: pointer;'});
			layout_body_cell_actions_delete.addEventListener(
				'click',
				function(event) {
					event.stop();
					const line = this.parentNode.parentNode;
					line.data.deleted = true;
					line.parentNode.removeChild(layout_expanded);
					line.parentNode.removeChild(layout_body_line);
				}
			);
			layout_body_cell_actions.appendChild(layout_body_cell_actions_delete);
			layout_body_line.appendChild(layout_body_cell_actions);

			//add one hidden line to display dataset details
			const layout_expanded = document.createFullElement('tr', {style: 'display: none; margin-top: 1rem;'});
			const layout_expanded_cell = document.createFullElement('td', {colspan: meaningful_field_models.length + 2, style: 'padding: 0.5rem; border: 1px solid #ccc;'});
			layout_expanded.appendChild(layout_expanded_cell);
			layout_expanded_cell.appendChild(draw_single_layout());

			return [layout_body_line, layout_expanded];
		}

		const layout_html = document.createFullElement('div', {id: this.id});
		if(!Object.isEmpty(this.textBefore)) {
			const layout_text_before = document.createElement('div');
			layout_text_before.innerHTML = this.getLocalizedTextBefore(languages);
			layout_html.appendChild(layout_text_before);
		}
		if(this.type === LayoutType.MULTIPLE.name) {
			const layout_table = document.createFullElement('table', {style: 'width: 100%; margin-top: 1rem; margin-bottom: 0.5rem; border-collapse: collapse;'});
			layout_html.appendChild(layout_table);

			//find multiple datasets
			const layout_datasets = datasets ? datasets.filter(d => this.datasetModelId === d.datasetModelId) : [];

			//head
			const layout_head = document.createElement('thead');
			layout_table.appendChild(layout_head);
			const layout_head_line = document.createElement('tr');
			layout_head.appendChild(layout_head_line);
			//add one column to expand/collapse lines
			layout_head_line.appendChild(document.createFullElement('th', {style: 'padding: 0.5rem; width: 3rem; border: 1px solid #ccc;'}));
			for(let i = 0; i < meaningful_field_models.length; i++) {
				const field_model = meaningful_field_models[i];
				layout_head_line.appendChild(document.createFullElement('th', {style: 'padding: 0.5rem; border: 1px solid #ccc;'}, field_model.getLocalizedShortname(languages)));
			}
			//add one column for actions
			layout_head_line.appendChild(document.createFullElement('th', {style: 'padding: 0.5rem; width: 3rem; border: 1px solid #ccc;'}));

			//body
			const layout_body = document.createElement('tbody');
			layout_table.appendChild(layout_body);
			if(layout_datasets.isEmpty()) {
				const layout_empty_line = document.createElement('tr');
				layout_empty_line.appendChild(document.createFullElement('td', {colspan: meaningful_field_models.length + 2}, `No ${this.getDatasetModel().getLocalizedShortname(languages)} reported`));
				layout_body.appendChild(layout_empty_line);
			}
			else {
				layout_datasets.map(draw_lines).forEach(Element.prototype.appendChildren, layout_body);
			}

			//button
			const layout_add_button = document.createFullElement('button', {type: 'button', style: 'margin-bottom: 1rem;'}, `Add ${this.getDatasetModel().getLocalizedShortname(languages)}`);
			layout_add_button.addEventListener('click', function() {
				//remove empty message
				if(layout_datasets.isEmpty()) {
					layout_body.empty();
				}
				const dataset = new objects.Entities.Dataset({datasetModelId: that.datasetModelId, number: Math.floor(new Date().getTime() * Math.random())});
				datasets.push(dataset);
				layout_datasets.push(dataset);
				layout_body.appendChildren(draw_lines(dataset));
			});
			layout_html.appendChild(layout_add_button);
		}
		else {
			layout_html.appendChild(draw_single_layout());
		}
		if(!Object.isEmpty(this.textAfter)) {
			const layout_text_after = document.createElement('div');
			layout_text_after.innerHTML = this.getLocalizedTextAfter(languages);
			layout_html.appendChild(layout_text_after);
		}

		//display or not according to visibility criteria
		const other_layouts_cells = [];
		for(let i = 0, length = this.formModel.layouts.indexOf(this); i < length; i++) {
			other_layouts_cells.pushAll(this.formModel.layouts[i].getCells());
		}
		for(let i = 0; i < other_layouts_cells.length; i++) {
			const other_layout_cell = other_layouts_cells[i];
			for(let j = 0; j < other_layout_cell.visibilityCriteria.length; j++) {
				const other_layout_cell_vc = other_layout_cell.visibilityCriteria[j];
				if(other_layout_cell_vc.targetLayoutIds.includes(this.id)) {
					layout_html.style.display = other_layout_cell_vc.action === 'SHOW' ? 'none' : '';
				}
			}
		}

		return layout_html;
	}
}
