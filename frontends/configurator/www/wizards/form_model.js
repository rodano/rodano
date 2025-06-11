import {Effects} from '../tools/effects.js';
import {Config} from '../model_config.js';
import {Languages} from '../languages.js';
import {DatasetModel} from '../model/config/entities/dataset_model.js';
import {FormModel} from '../model/config/entities/form_model.js';
import {PossibleValue} from '../model/config/entities/possible_value.js';
import {Layout} from '../model/config/entities/layout.js';
import {Line} from '../model/config/entities/line.js';
import {Cell} from '../model/config/entities/cell.js';
import {FieldModel} from '../model/config/entities/field_model.js';
import {StudyHandler} from '../study_handler.js';
import {Wizards} from '../wizards.js';
import {Entities} from '../model/config/entities.js';
import {FormHelpers} from '../form_helpers.js';
import {UI} from '../tools/ui.js';
import {Router} from '../router.js';
import {bus} from '../model/config/entities_hooks.js';

let form_model;
let dataset_model;

let selected_cell;

function get_selected_layout() {
	if(form_model.layouts.isEmpty()) {
		//create new layout
		const layout = new Layout({
			formModel: form_model,
			id: form_model.id
		});
		form_model.addChild(layout);
	}
	return form_model.layouts[0];
}

function sort_save() {
	const layout = get_selected_layout();
	//sort layout lines
	const cellIds = document.getElementById('wizard_form_model_cells').children.map(c => c.dataset.cellId);
	layout.lines.sort((line_1, line_2) => {
		const cell_1 = line_1.cells[0];
		const cell_2 = line_2.cells[0];
		return cellIds.indexOf(cell_1.id) - cellIds.indexOf(cell_2.id);
	});

	//sort field models
	for(let i = layout.lines.length - 1; i >= 0; i--) {
		const cell = layout.lines[i].cells[0];
		if(cell.hasFieldModel()) {
			dataset_model.getFieldModel(cell.fieldModelId).exportOrder = i * 5;
		}
	}
	dataset_model.fieldModels.sort(FieldModel.getComparator());
}

function select_cell(cell) {
	//select in model
	selected_cell = cell;
	//select in ui
	const wizard_form_model_cells = document.getElementById('wizard_form_model_cells');
	wizard_form_model_cells.children.forEach(e => e.classList.remove('selected'));
	const cell_ui = wizard_form_model_cells.querySelector(`div[data-cell-id="${cell.id}"]`);
	cell_ui.classList.add('selected');
	//manage cell details
	const wizard_form_model_field_model_label = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_form_model_field_model_label'));
	if(cell.hasFieldModel()) {
		//do not use Cell:getFieldModel to retrieve its field model because the dataset model has not been added in the study yet
		const field_model = dataset_model.fieldModels.find(f => f.id === cell.fieldModelId);

		wizard_form_model_field_model_label.value = !Object.isEmpty(field_model.shortname) ? field_model.getLocalizedShortname(Languages.GetLanguage()) : '';

		const wizard_form_model_field_model_size = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_field_model_size'));
		if(field_model.type === 'STRING') {
			wizard_form_model_field_model_size.value = field_model.size;
			wizard_form_model_field_model_size.parentElement.style.display = 'block';
		}
		else {
			wizard_form_model_field_model_size.parentElement.style.display = 'none';
		}

		const wizard_form_model_field_model_format = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_field_model_format'));
		const wizard_form_model_field_model_unit = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_form_model_field_model_unit'));
		if(field_model.type === 'NUMBER') {
			wizard_form_model_field_model_format.value = field_model.format || '';
			wizard_form_model_field_model_unit.value = field_model.forDisplay || '';
			wizard_form_model_field_model_unit.parentElement.style.display = 'block';
		}
		else {
			wizard_form_model_field_model_unit.parentElement.style.display = 'none';
			wizard_form_model_field_model_format.parentElement.style.display = 'none';
		}

		const wizard_form_model_field_model_date_format = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_field_model_date_format'));
		if(field_model.type === 'DATE') {
			wizard_form_model_field_model_date_format.value = field_model.format;
			wizard_form_model_field_model_date_format.parentElement.style.display = 'block';
		}
		else {
			wizard_form_model_field_model_date_format.parentElement.style.display = 'none';
		}

		const wizard_form_model_field_model_possible_values = document.getElementById('wizard_form_model_field_model_possible_values');
		wizard_form_model_field_model_possible_values.empty();
		if(Config.Enums.FieldModelType[field_model.type].is_multiple_choice) {
			field_model.possibleValues.map(draw_possible_value).forEach(Node.prototype.appendChild, wizard_form_model_field_model_possible_values);
			wizard_form_model_field_model_possible_values.parentElement.style.display = 'block';
		}
		else {
			wizard_form_model_field_model_possible_values.parentElement.style.display = 'none';
		}
	}
	else {
		wizard_form_model_field_model_label.value = !Object.isEmpty(cell.textBefore) ? cell.getLocalizedTextBefore(Languages.GetLanguage()) : '';
	}
	document.getElementById('wizard_form_model_field_details').style.visibility = 'visible';
}

function draw_cell(cell) {
	let cell_ui = document.createFullElement('div');
	cell_ui.appendChild(document.createFullElement('div', cell.textBefore[Languages.GetLanguage()] || ''));
	if(cell.hasFieldModel()) {
		const field_model = dataset_model.getFieldModel(cell.fieldModelId);
		cell_ui = field_model.createFullHTML(cell, Languages.GetLanguage());
		//add icon and format for date
		if(field_model.type === 'DATE') {
			cell_ui.appendChild(document.createFullElement('img', {src: 'wizards/date.png', style: 'margin-left: 1rem;'}));
		}
	}
	cell_ui.appendChild(document.createFullElement('div', cell.textAfter[Languages.GetLanguage()] || ''));

	//listeners
	cell_ui.dataset.cellId = cell.id;
	cell_ui.addEventListener('click', () => select_cell(cell));

	//add sort image
	const sort = document.createFullElement('img', {src: 'wizards/arrows_up_down.png', alt: 'Sort'});
	cell_ui.insertBefore(sort, cell_ui.firstChild);

	return cell_ui;
}

function draw_possible_value(possible_value) {
	const possible_value_ui = document.createFullElement('li');
	//input
	possible_value_ui.appendChild(document.createFullElement('input', {value: !Object.isEmpty(possible_value.shortname) ? possible_value.getLocalizedShortname(Languages.GetLanguage()) : ''}));
	//delete possible value button
	const delete_possible_value = document.createFullElement('img', {src: 'wizards/cross.png'});
	delete_possible_value.addEventListener(
		'click',
		function() {
			const possible_value_ui = this.parentElement;
			//update model
			const field_model = dataset_model.fieldModels.find(f => f.id === selected_cell.fieldModelId);
			field_model.possibleValues.splice(possible_value_ui.parentElement.children.indexOf(possible_value_ui), 1);
			//update ui
			possible_value_ui.parentElement.removeChild(possible_value_ui);
		}
	);
	possible_value_ui.appendChild(delete_possible_value);
	return possible_value_ui;
}

function update_layout() {
	const wizard_form_model_cells = document.getElementById('wizard_form_model_cells');
	wizard_form_model_cells.empty();
	get_selected_layout().getCells().map(draw_cell).forEach(Node.prototype.appendChild, wizard_form_model_cells);
}

Wizards.Register('form_model', {
	title: 'New form model',
	description: 'This wizard will help you to create a form model.',
	steps: 3,
	mode: Wizards.Mode.FULLSCREEN,
	labels: {
		'1': 'Create form model',
		'3': 'Close'
	},
	no_return: 3,
	init: function() {
		//prepare sort
		Effects.Sortable(document.getElementById('wizard_form_model_cells'), sort_save);

		document.getElementById('wizard_form_model_field_model_add_possible_value').addEventListener(
			'click',
			function(event) {
				event.stop();
				const field_model = dataset_model.fieldModels.find(f => f.id === selected_cell.fieldModelId);
				const possible_value = new PossibleValue({
					fieldModel: field_model
				});
				field_model.possibleValues.push(possible_value);
				document.getElementById('wizard_form_model_field_model_possible_values').appendChild(draw_possible_value(possible_value));
			}
		);

		function drag_start(event) {
			event.dataTransfer.effectAllowed = 'linkMove';
			event.dataTransfer.setData('text', JSON.stringify({type: this.dataset.type, dataType: this.dataset.dataType}));
		}

		function drag_enter_over(event) {
			event.preventDefault();
			this.classList.add('dragover');
		}

		function drag_leave() {
			this.classList.remove('dragover');
		}

		document.querySelectorAll('#wizard_form_model_field_model_types > li').forEach(function(element) {
			element.setAttribute('draggable', true);
			element.addEventListener('dragstart', drag_start);
		});
		document.getElementById('wizard_form_model_content').addEventListener('dragenter', drag_enter_over);
		document.getElementById('wizard_form_model_content').addEventListener('dragover', drag_enter_over);
		document.getElementById('wizard_form_model_content').addEventListener('dragleave', drag_leave);

		document.getElementById('wizard_form_model_content').addEventListener(
			'drop',
			function(event) {
				event.preventDefault();
				this.classList.remove('dragover');

				bus.disable();

				const layout = get_selected_layout();
				//create line and cell
				const line = new Line({layout: layout});
				layout.addChild(line);
				const cell = new Cell({
					line: line,
					id: (`CELL_${new Date().getTime()}`),
					displayPossibleValueLabels: true
				});
				line.addChild(cell);

				const data = JSON.parse(event.dataTransfer.getData('text'));
				//update model
				if(data.type && data.dataType) {
					//create field model
					const field_model = new FieldModel({
						datasetModel: dataset_model,
						id: `${dataset_model.generateFieldModelId()}_${new Date().getTime()}`,
						shortname: {
							[Languages.GetLanguage()]: 'Untitled',
						},
						type: data.type,
						dataType: data.dataType,
						exportable: true,
						exportOrder: dataset_model.fieldModels.length > 0 ? dataset_model.fieldModels.last().exportOrder + 5 : 0
					});
					//adjust field model properties
					if(data.type === 'STRING') {
						field_model.size = 25;
					}

					if(field_model.type === 'RADIO' || field_model.type === 'SELECT') {
						for(let i = 1; i < 4; i++) {
							const possible_value = new PossibleValue({
								fieldModel: field_model,
								id: `CHOICE_${i}`,
								shortname: {
									[Languages.GetLanguage()]: `Choice ${i}`
								}
							});
							field_model.possibleValues.push(possible_value);
						}
					}
					field_model.datasetModel = dataset_model;
					dataset_model.addChild(field_model);

					cell.datasetModelId = dataset_model.id;
					cell.fieldModelId = field_model.id;
				}
				else {
					cell.cssCode = 'height: 20px;';
				}

				bus.enable();

				//update ui
				const cell_ui = draw_cell(cell);
				const wizard_form_model_cells = document.getElementById('wizard_form_model_cells');
				wizard_form_model_cells.appendChild(cell_ui);
				select_cell(cell);
			}
		);

		document.getElementById('wizard_form_model_field_details').addEventListener(
			'submit',
			/**@this HTMLFormElement*/
			function(event) {
				event.stop();
				if(selected_cell.hasFieldModel()) {
					const field_model = dataset_model.getFieldModel(selected_cell.fieldModelId);
					//save shortname
					field_model.shortname = {};
					field_model.shortname[Languages.GetLanguage()] = this.label.value;
					//save specific parameters
					if(field_model.type === 'STRING') {
						field_model.size = parseInt(this.size.value);
						field_model.maxLength = field_model.size;
					}
					if(field_model.type === 'NUMBER') {
						field_model.format = this.number_format.value;
						field_model.forDisplay = this.unit.value;
						field_model.size = 4 + field_model.format.length;
						field_model.maxLength = field_model.size;
					}
					if(field_model.type === 'DATE') {
						const format = this.date_format.value;
						field_model.format = format;
						field_model.forDisplay = `(${format.toLowerCase()})`;
						field_model.size = format.length;
						field_model.maxLength = field_model.size;
					}
					//save possible values
					if(Config.Enums.FieldModelType[field_model.type].is_multiple_choice) {
						const possible_values_ui = document.getElementById('wizard_form_model_field_model_possible_values');
						const possible_values = [];
						for(let i = 0; i < possible_values_ui.children.length; i++) {
							const possible_value_input = possible_values_ui.children[i].querySelector('input');
							if(possible_value_input.value) {
								const possible_value = new PossibleValue();
								possible_value.fieldModel = field_model;
								possible_value.shortname = {};
								possible_value.shortname[Languages.GetLanguage()] = possible_value_input.value;
								const id = possible_value.shortname[Languages.GetLanguage()].toUpperCase().replace(/ /g, '_');
								possible_value.id = id;
								possible_values.push(possible_value);
							}
						}
						field_model.possibleValues = possible_values;
					}
				}
				else {
					//save text before
					selected_cell.textBefore = {};
					selected_cell.textBefore[Languages.GetLanguage()] = this.label.value;
				}

				//update ui
				this.style.visibility = 'hidden';
				update_layout();
			}
		);

		document.getElementById('wizard_form_model_field_delete').addEventListener(
			'click',
			function() {
				UI.Validate('Are you sure you want to delete this field?').then(confirmed => {
					if(confirmed) {
						//remove field model from dataset model if needed
						if(selected_cell.hasFieldModel()) {
							const field_model = dataset_model.getFieldModel(selected_cell.fieldModelId);
							dataset_model.fieldModels.removeElement(field_model);
						}
						//remove line from layout
						get_selected_layout().lines.removeElement(selected_cell.line);

						//update ui
						document.getElementById('wizard_form_model_field_details').style.visibility = 'hidden';
						update_layout();
					}
				});
			}
		);

		const study = StudyHandler.GetStudy();
		FormHelpers.FillSelect(document.getElementById('wizard_form_model_scope_model_id'), study.scopeModels, true);

		document.getElementById('wizard_form_model_scope_model_id').addEventListener('change', function() {
			const scope_model_id = /**@type {HTMLSelectElement}*/ (this).value;
			if(scope_model_id) {
				const scope_model = study.getScopeModel(scope_model_id);
				FormHelpers.FillSelect(document.getElementById('wizard_form_model_event_model_id'), scope_model.eventModels, false);
			}
		});
	},
	onStart() {
		const study = StudyHandler.GetStudy();
		//find suitable form model id
		const node_base_id = 'NEW_FORM_MODEL';
		const node_base_shortname = 'New form model';
		let node_id = node_base_id, node_shortname = node_base_shortname;
		let i = 2;
		while(study.getHasChild(Entities.FormModel, undefined, node_id)) {
			node_id = `${node_base_id}_${i}`;
			node_shortname = `${node_base_shortname} ${i}`;
			i++;
		}
		//create form model and dataset model but do not add them in the study yet
		form_model = new FormModel({
			id: node_id,
			shortname: {
				en: node_shortname
			}
		});
		dataset_model = new DatasetModel({
			id: node_id,
			shortname: {
				en: node_shortname
			}
		});
		//reset fields
		document.getElementById('wizard_form_model_field_details').style.visibility = 'hidden';
		selected_cell = undefined;
		document.getElementById('wizard_form_model_cells').empty();
	},
	onValidate: function(step) {
		switch(step) {
			case 2: {
				if(!/**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_scope_model_id')).value) {
					document.getElementById('wizard_error').textContent = 'A scope model is required';
					document.getElementById('wizard_error').style.display = 'block';
					return false;
				}
			}
		}
		return true;
	},
	onNext: function(step) {
		const study = StudyHandler.GetStudy();
		switch(step) {
			case 2: {
				form_model.study = study;
				study.addChild(form_model);
				dataset_model.study = study;
				study.addChild(dataset_model);
				const scope_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_scope_model_id')).value;
				const scope_model = study.getScopeModel(scope_model_id);
				const event_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('wizard_form_model_event_model_id')).value;
				const entity = event_model_id ? scope_model.getEventModel(event_model_id) : scope_model;
				entity.formModelIds.push(form_model.id);
				entity.datasetModelIds.push(dataset_model.id);
				break;
			}
		}
	},
	onEnd: function() {
		Router.SelectNode(form_model);
	}
});
