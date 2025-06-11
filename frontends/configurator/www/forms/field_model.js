import '../basic-tools/extension.js';

import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {Config, ConfigHelpers} from '../model_config.js';
import {bus} from '../model/config/entities_hooks.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {ConstraintHelpers} from '../constraint_helpers.js';
import {bus_ui} from '../bus_ui.js';
import {Entities} from '../model/config/entities.js';
import {PossibleValue} from '../model/config/entities/possible_value.js';

//specific cases
function toggle_section_type(section, type) {
	const types = Array.prototype.slice.call(arguments, 2);
	section.style.display = types.includes(type) ? 'block' : 'none';
}

function manage_type(event) {
	const field_model_type = Config.Enums.FieldModelType[event ? this.value : selected_field_model.type];

	const field_model_max_length = /**@type {HTMLInputElement}*/ (document.getElementById('field_model_max_length'));

	//do special initialization when type is modified
	if(event) {
		//sizable
		if(!field_model_type.is_sizable) {
			field_model_max_length.value = '';
		}
	}

	//update form in any cases
	//multiple choice
	/**@type {HTMLAnchorElement}*/
	const possible_values_link = document.querySelector('#edit_field_model li > a[data-tab="edit_field_model_possible_values"]');
	const field_model_possible_values = document.getElementById('field_model_possible_values');
	if(field_model_type.is_multiple_choice) {
		field_model_possible_values.style.display = 'block';
		if(possible_values_link.parentElement.children.length === 2) {
			possible_values_link.parentElement.removeChild(possible_values_link.nextElementSibling);
		}
		possible_values_link.parentElement.classList.remove('disabled');
		possible_values_link.style.display = 'inline-block';
	}
	else {
		field_model_possible_values.style.display = 'none';
		possible_values_link.style.display = 'none';
		possible_values_link.parentElement.classList.add('disabled');
		if(possible_values_link.parentElement.children.length === 1) {
			possible_values_link.parentElement.appendChild(document.createFullElement('span', {}, possible_values_link.textContent));
		}
	}
	//sizable
	if(field_model_type.is_sizable) {
		field_model_max_length.parentElement.style.display = 'block';
	}
	else {
		field_model_max_length.parentElement.style.display = 'none';
	}

	toggle_section_type(document.getElementById('field_model_number'), field_model_type, Config.Enums.FieldModelType.NUMBER);
	toggle_section_type(document.getElementById('field_model_date'), field_model_type, Config.Enums.FieldModelType.DATE, Config.Enums.FieldModelType.DATE_SELECT);
	toggle_section_type(document.getElementById('field_model_date_select'), field_model_type, Config.Enums.FieldModelType.DATE_SELECT);
	toggle_section_type(document.getElementById('field_model_allow_date_in_future').parentNode, field_model_type, Config.Enums.FieldModelType.DATE, Config.Enums.FieldModelType.DATE_SELECT);
	toggle_section_type(document.getElementById('field_model_min_value').parentNode, field_model_type, Config.Enums.FieldModelType.NUMBER);
	toggle_section_type(document.getElementById('field_model_max_value').parentNode, field_model_type, Config.Enums.FieldModelType.NUMBER);
	toggle_section_type(document.getElementById('field_model_min_year').parentNode, field_model_type, Config.Enums.FieldModelType.DATE, Config.Enums.FieldModelType.DATE_SELECT);
	toggle_section_type(document.getElementById('field_model_max_year').parentNode, field_model_type, Config.Enums.FieldModelType.DATE, Config.Enums.FieldModelType.DATE_SELECT);
	toggle_section_type(document.getElementById('field_model_max_length').parentNode, field_model_type, Config.Enums.FieldModelType.STRING, Config.Enums.FieldModelType.TEXTAREA, Config.Enums.FieldModelType.FILE);
	toggle_section_type(document.getElementById('field_model_matcher').parentNode, field_model_type, Config.Enums.FieldModelType.STRING, Config.Enums.FieldModelType.NUMBER);
	toggle_section_type(document.getElementById('field_model_matcher_message').parentNode, field_model_type, Config.Enums.FieldModelType.STRING, Config.Enums.FieldModelType.NUMBER);
}

function manage_data_type(event) {
	const field_model_data_type = Config.Enums.DataType[/**@type {HTMLSelectElement}*/ (document.getElementById('field_model_data_type')).value];

	//retrieve some fields
	const field_model_min_value = /**@type {HTMLInputElement}*/ (document.getElementById('field_model_min_value'));
	const field_model_max_value = /**@type {HTMLInputElement}*/ (document.getElementById('field_model_max_value'));

	//do special initialization when type is modified
	if(event) {
		//number
		if(field_model_data_type !== Config.Enums.FieldModelType.DATE) {
			field_model_min_value.value = '';
			field_model_max_value.value = '';
		}
	}
}

function manage_specifys() {
	const specifys = document.querySelectorAll('#field_model_possible_values input[data-name="specify"]');
	const one_checked = specifys.some(i => i.checked);
	document.querySelectorAll('#field_model_possible_values input[data-name="specify"]').forEach(function(input) {
		if(input.checked || !one_checked) {
			input.parentElement.style.display = 'block';
		}
		else {
			input.parentElement.style.display = 'none';
		}
	});
}

function delete_possible_value(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this possible value?').then(confirmed => {
		if(confirmed) {
			const possible_value_div = this.parentNode.parentNode;
			possible_value_div.possibleValue.delete();
			possible_value_div.parentNode.removeChild(possible_value_div);
		}
	});
}

function draw_possible_value(possible_value) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('field_model_possible_value')).content, true);

	instance.querySelector('div').possibleValue = possible_value;
	instance.querySelector('button').addEventListener('click', delete_possible_value);
	instance.querySelector('span').textContent = possible_value.id || '';

	const field_model_possible_value_id = /**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="id"]'));
	const field_model_possible_value_shortname = /**@type {AppLocalizedInput}*/ (instance.querySelector('app-localized-input[data-name="shortname"]'));
	const field_model_possible_value_export_label = /**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="export_label"]'));
	const field_model_possible_value_specify = /**@type {HTMLInputElement}*/ (instance.querySelector('input[data-name="specify"]'));

	FormHelpers.FillLocalizedInput(field_model_possible_value_shortname, selected_field_model.datasetModel.study.languages);

	field_model_possible_value_id.addEventListener(
		'input',
		function() {
			this.setCustomValidity('');
			//look in other possible values
			if(document.querySelectorAll('#field_model_possible_values input[data-name="id"]').some(i => this !== i && this.value === i.value)) {
				this.setCustomValidity('There is already a possible value with this id');
			}
		}
	);

	field_model_possible_value_id.value = possible_value.id || '';
	field_model_possible_value_shortname.value = JSON.stringify(possible_value.shortname);
	field_model_possible_value_export_label.value = possible_value.exportLabel || '';
	field_model_possible_value_specify.checked = possible_value.specify;

	const field_model_type = Config.Enums.FieldModelType[/**@type {HTMLSelectElement}*/ (document.getElementById('field_model_type')).value];
	if(field_model_type.has_multiple_values) {
		field_model_possible_value_export_label.parentElement.parentElement.style.display = 'block';
	}
	else {
		field_model_possible_value_export_label.parentElement.parentElement.style.display = 'none';
	}
	field_model_possible_value_specify.addEventListener('change', manage_specifys);
	field_model_possible_value_specify.parentElement.parentElement.style.display = [Config.Enums.FieldModelType.CHECKBOX_GROUP].includes(field_model_type) ? 'block' : 'none';

	field_model_possible_value_id.value = possible_value.id || '';
	field_model_possible_value_shortname.value = JSON.stringify(possible_value.shortname);
	field_model_possible_value_export_label.value = possible_value.exportLabel || '';
	field_model_possible_value_specify.checked = possible_value.specify;

	return instance;
}

let selected_field_model;

export default {
	form: 'edit_field_model_form',
	init: function() {
		function bus_register() {
			bus.register({
				onChangePossibleValue: function(event) {
					if(event.node.fieldModel === selected_field_model && ['id'].includes(event.property)) {
						const possible_value_ui = document.querySelectorAll('#field_model_possible_values > div').find(e => e.possibleValue === event.node);
						//update id
						possible_value_ui.querySelector('h3 > span').textContent = event.node.id;
					}
				}
			});
		}
		//register hook when a study is loaded because the bus will be reset at that time
		bus_ui.register({onLoadStudy: bus_register});
		bus_register();

		document.getElementById('field_model_copy_possible_values').addEventListener(
			'click',
			function(event) {
				event.preventDefault();
				const field_model_global_id = /**@type {HTMLSelectElement}*/ (document.getElementById('field_model_copy_possible_values_field_models')).value;
				const field_model = selected_field_model.datasetModel.study.getNode(field_model_global_id);
				selected_field_model.possibleValues = field_model.possibleValues.map(function(possible_value) {
					//clone possible value and keep possible value id
					const clone_possible_value = ConfigHelpers.CloneNode(possible_value, {id: possible_value.id});
					clone_possible_value.fieldModel = selected_field_model;
					return clone_possible_value;
				});
				selected_field_model.possibleValues.map(draw_possible_value).forEach(Node.prototype.appendChild, document.getElementById('field_model_possible_values').empty('div'));
				manage_specifys();
			}
		);

		document.getElementById('edit_field_model_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_field_model, this['id'].value)) {
					FormHelpers.UpdateObject(selected_field_model, this);

					const field_model_type = Config.Enums.FieldModelType[selected_field_model.type];
					document.querySelectorAll('#field_model_possible_values > div').forEach(function(possible_value_div) {
						const possible_value = possible_value_div.possibleValue;
						possible_value.id = possible_value_div.querySelector('input[data-name="id"]').value;
						possible_value.shortname = JSON.parse(possible_value_div.querySelector('app-localized-input[data-name="shortname"]').value);
						if(field_model_type.has_multiple_values) {
							possible_value.exportLabel = possible_value_div.querySelector('input[data-name="export_label"]').value;
						}
						else {
							possible_value.exportLabel = undefined;
						}
						if([Config.Enums.FieldModelType.CHECKBOX_GROUP].includes(field_model_type)) {
							possible_value.specify = possible_value_div.querySelector('input[data-name="specify"]').checked;
						}
						else {
							possible_value.specify = false;
						}
					});

					FormStaticActions.AfterSubmission(selected_field_model);
				}
			}
		);

		document.getElementById('field_model_type').addEventListener('change', manage_type);
		document.getElementById('field_model_data_type').addEventListener('change', manage_data_type);

		document.getElementById('field_model_possible_values_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const possible_value = new PossibleValue();
				possible_value.fieldModel = selected_field_model;
				selected_field_model.possibleValues.push(possible_value);
				document.getElementById('field_model_possible_values').appendChild(draw_possible_value(possible_value));
				manage_specifys();
			}
		);

		document.getElementById('field_model_possible_values_sort').addEventListener(
			'click',
			function(event) {
				event.stop();
				selected_field_model.possibleValues.sort(function(possible_value_1, possible_value_2) {
					return possible_value_1.getLocalizedShortname(Languages.GetLanguage()).compareTo(possible_value_2.getLocalizedShortname(Languages.GetLanguage()));
				});
				//sort possible values representations
				const possible_values_container = document.getElementById('field_model_possible_values');
				const possible_values_divs = possible_values_container.querySelectorAll(':scope > div');
				selected_field_model.possibleValues.forEach(possible_value => {
					possible_values_container.appendChild(possible_values_divs.find(e => e.possibleValue === possible_value));
				});
				UI.Notify('Possible values sorted successfully', {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		);

		Effects.Sortable(
			document.getElementById('field_model_possible_values'),
			function() {
				selected_field_model.possibleValues = this.querySelectorAll(':scope > div').map(c => c.possibleValue);
			},
			'h3 > img:first-child',
			undefined,
			'div'
		);

		FormStaticActions.ManageConstraintEdition('valueConstraint', document.getElementById('field_model_value_constraint_add'), document.getElementById('field_model_value_constraint_edit'), document.getElementById('field_model_value_constraint_delete'));
	},
	open: function(field_model) {
		selected_field_model = field_model;

		FormHelpers.FillSelectEnum(document.getElementById('field_model_type'), Config.Enums.FieldModelType);
		FormHelpers.FillSelectEnum(document.getElementById('field_model_data_type'), Config.Enums.DataType);
		FormHelpers.FillPalette(document.getElementById('field_model_validator_ids'), field_model.datasetModel.study.validators);
		FormHelpers.FillPalette(document.getElementById('field_model_workflow_ids'), field_model.datasetModel.study.workflows);
		FormHelpers.FillLocalizedInput(document.getElementById('field_model_shortname'), field_model.datasetModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('field_model_longname'), field_model.datasetModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('field_model_description'), field_model.datasetModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('field_model_matcher_message'), field_model.datasetModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('field_model_advanced_help'), field_model.datasetModel.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_field_model_form'), field_model);
		const possible_values_field_models = field_model.datasetModel.study.getDescendants(Entities.FieldModel).filter(f => f !== field_model && !f.possibleValues.isEmpty());
		possible_values_field_models.sort(field_model.constructor.getComparator(Languages.GetLanguage()));
		FormHelpers.FillSelect(document.getElementById('field_model_copy_possible_values_field_models'), possible_values_field_models, false, undefined, {value_property: 'getGlobalId'});

		manage_type();
		manage_data_type();

		NodeTools.DrawUsage(field_model, document.getElementById('field_model_usage'));

		field_model.possibleValues.map(draw_possible_value).forEach(Node.prototype.appendChild, document.getElementById('field_model_possible_values').empty('div'));
		manage_specifys();

		FormStaticActions.DrawRules(field_model, field_model.rules, field_model.constructor.RuleEntities, document.getElementById('field_model_modification_rules'), 'Modification rules');

		FormStaticActions.UpdateConstraintEdition(field_model, 'valueConstraint', document.getElementById('field_model_value_constraint_add'), document.getElementById('field_model_value_constraint_edit'), document.getElementById('field_model_value_constraint_delete'));

		//fill value formula datalist
		if(field_model.valueConstraint) {
			ConstraintHelpers.EnhanceValueInput(field_model.valueConstraint, /**@type {HTMLInputElement}*/ (document.getElementById('field_model_value_formula')));
		}
	}
};
