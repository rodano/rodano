import '../basic-tools/extension.js';

import {UI} from '../tools/ui.js';
import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {Router} from '../router.js';
import {VisibilityCriteria} from '../model/config/entities/visibility_criteria.js';

function delete_visibility_criterion(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this visibility criterion?').then(confirmed => {
		if(confirmed) {
			const visibility_criterion_div = this.parentNode;
			visibility_criterion_div.visibilityCriterion.delete();
			visibility_criterion_div.parentNode.removeChild(visibility_criterion_div);
		}
	});
}

function draw_visibility_criterion(visibility_criterion) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('cell_visibility_criterion')).content, true);

	instance.querySelector('div').visibilityCriterion = visibility_criterion;
	instance.querySelector('button').addEventListener('click', delete_visibility_criterion);

	const layout = visibility_criterion.cell.line.layout;
	const field_model = visibility_criterion.cell.getFieldModel();
	const field_model_type = Config.Enums.FieldModelType[field_model.type];

	//values select
	if(field_model_type.is_multiple_choice || field_model_type === Config.Enums.FieldModelType.CHECKBOX) {
		const values_options = field_model_type.is_multiple_choice ? field_model.possibleValues : [
			{
				id: 'true',
				shortname: {'en': 'True'}
			},
			{
				id: 'false',
				shortname: {'en': 'False'}
			}
		];
		instance.querySelector('select[data-name="operator"]').remove();
		instance.querySelector('input[data-name="values"]').remove();
		FormHelpers.FillSelect(instance.querySelector('select[data-name="values"]'), values_options, false, visibility_criterion.values, {value_property: 'id', label_property: 'shortname'});
	}
	else {
		instance.querySelector('select[data-name="values"]').remove();
		const operators = Object.fromEntries(field_model_type.operators.map(o => [o.name, o]));
		FormHelpers.FillSelectEnum(instance.querySelector('select[data-name="operator"]'), operators, true, visibility_criterion.operator);
		instance.querySelector('input[data-name="values"]').value = visibility_criterion.values[0] || '';
	}

	//action
	FormHelpers.FillSelectEnum(instance.querySelector('select[data-name="action"]'), Config.Enums.VisibilityCriteriaAction, false, visibility_criterion.action);

	//target selects
	const select_options = {
		label_property: 'id',
		disable_auto_sort: true
	};

	//target layouts select
	//only cell in non repeatable layout can target a layout
	let layouts = [];
	if(!Config.Enums.LayoutType[layout.type].repeatable) {
		layouts = layout.formModel.layouts.slice();
		layouts.removeElement(layout);
	}
	const visibility_criterion_target_layout_ids = /**@type {HTMLSelectElement}*/ (instance.querySelector('select[data-name="targetLayoutIds"]'));
	FormHelpers.FillSelect(visibility_criterion_target_layout_ids, layouts, false, visibility_criterion.targetLayoutIds, select_options);
	visibility_criterion_target_layout_ids.style.display = layouts.isEmpty() ? 'none' : 'inline';

	//target cells target
	const cells = layout.getCells();
	cells.removeElement(visibility_criterion.cell);
	const visibility_criterion_target_cell_ids = /**@type {HTMLSelectElement}*/ (instance.querySelector('select[data-name="targetCellIds"]'));
	FormHelpers.FillSelect(visibility_criterion_target_cell_ids, cells, false, visibility_criterion.targetCellIds, select_options);
	visibility_criterion_target_cell_ids.style.display = cells.isEmpty() ? 'none' : 'inline';

	return instance;
}

function close_form() {
	Router.SelectNode(selected_cell.line.layout.formModel, 'edit_form_model_content');
}

let selected_cell;

export default {
	form: 'edit_cell_form',
	init: function() {
		document.getElementById('cell_visibility_criterion_add').addEventListener(
			'click',
			function() {
				const visibility_criterion = new VisibilityCriteria();
				visibility_criterion.cell = selected_cell;
				selected_cell.visibilityCriteria.push(visibility_criterion);
				document.getElementById('cell_visibility_criteria').appendChild(draw_visibility_criterion(visibility_criterion));
			}
		);

		document.getElementById('edit_cell_form')['id'].addEventListener(
			'input',
			function() {
				//check cell ids
				const cells = selected_cell.line.layout.formModel.getCells();
				cells.removeElement(selected_cell);
				const cell_ids = cells.map(c => c.id);
				if(cell_ids.includes(this.value)) {
					this.setCustomValidity(`Id "${this.value}" already exists`);
				}
				else {
					//remove error if any
					this.setCustomValidity('');
				}
			}
		);

		document.getElementById('edit_cell_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_cell, this['id'].value)) {
					FormHelpers.UpdateObject(selected_cell, this);
					//update cell id if necessary
					/*if(cell.id === 'EMPTY_CELL') {
						var identify = function(text) {
							var id = text.toUpperCase().replace(/ /g,'_').replace(/\./g,'_');
							var words = id.split('_');
							if(words.length < 3) {
								return id;
							}
							//keep first two words
							words.length = 3;
							return words.join('_');
						};
						if(cell_text_zones[0].textContent) {
							cell.id = identify(cell_text_zones[0].textContent);
						}
						else if(cell_text_zones[1].textContent) {
							cell.id = identify(cell_text_zones[1].textContent);
						}
					}*/
					//update cell content when there is only text?
					if(selected_cell.hasFieldModel()) {
						const field_model = selected_cell.getFieldModel();
						const field_model_type = Config.Enums.FieldModelType[field_model.type];
						document.querySelectorAll('#cell_visibility_criteria > div').forEach(function(visibility_criterion_div) {
							const visibility_criterion = visibility_criterion_div.visibilityCriterion;
							if(field_model_type.is_multiple_choice || field_model_type === Config.Enums.FieldModelType.CHECKBOX) {
								visibility_criterion.operator = undefined;
								visibility_criterion.values = visibility_criterion_div.querySelector('select[data-name="values"]').selectedOptions.map(o => o.value);
							}
							else {
								visibility_criterion.operator = visibility_criterion_div.querySelector('select[data-name="operator"]').value;
								visibility_criterion.values = [visibility_criterion_div.querySelector('input[data-name="values"]').value];
							}
							visibility_criterion.action = visibility_criterion_div.querySelector('select[data-name="action"]').value;
							visibility_criterion.targetLayoutIds = visibility_criterion_div.querySelector('select[data-name="targetLayoutIds"]').selectedOptions.map(o => o.value);
							visibility_criterion.targetCellIds = visibility_criterion_div.querySelector('select[data-name="targetCellIds"]').selectedOptions.map(o => o.value);
						});
					}

					FormStaticActions.AfterSubmission(selected_cell);
					close_form();
				}
			}
		);

		document.getElementById('edit_cell_close').addEventListener('click', close_form);

		FormStaticActions.ManageConstraintEdition('constraint', document.getElementById('cell_constraint_add'), document.getElementById('cell_constraint_edit'), document.getElementById('cell_constraint_delete'));
	},
	open: function(cell) {
		selected_cell = cell;

		FormHelpers.FillLocalizedInput(document.getElementById('cell_text_before'), cell.line.layout.formModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('cell_text_after'), cell.line.layout.formModel.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_cell_form'), cell);

		FormStaticActions.UpdateConstraintEdition(cell, 'constraint', document.getElementById('cell_constraint_add'), document.getElementById('cell_constraint_edit'), document.getElementById('cell_constraint_delete'));

		//toggle visibility criteria
		const cell_visibility = document.getElementById('cell_visibility');
		cell_visibility.style.display = 'none';
		if(cell.hasFieldModel()) {
			if(Config.Enums.FieldModelType[cell.getFieldModel().type].has_visibility_criteria) {
				cell_visibility.style.display = 'block';
			}
		}

		//adjust properties
		if(cell.hasFieldModel()) {
			document.getElementById('cell_css_code_for_label').removeAttribute('disabled');
			document.getElementById('cell_css_code_for_input').removeAttribute('disabled');
		}
		else {
			document.getElementById('cell_css_code_for_label').setAttribute('disabled', 'disabled');
			document.getElementById('cell_css_code_for_input').setAttribute('disabled', 'disabled');
		}

		cell.visibilityCriteria.map(draw_visibility_criterion).forEach(Node.prototype.appendChild, document.getElementById('cell_visibility_criteria').empty('div'));
	}
};
