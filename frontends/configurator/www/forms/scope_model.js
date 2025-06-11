import {UI} from '../tools/ui.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {WorkflowStatesSelector} from '../model/config/entities/workflow_states_selector.js';

function dragstart(event) {
	this.style.opacity = 0.6;
	event.dataTransfer.effectAllowed = 'copy';
	event.dataTransfer.setData('text/plain', this.textContent);
}

function dragend() {
	this.style.opacity = 1;
}

function delete_selector(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this selector?').then(confirmed => {
		if(confirmed) {
			const selector_div = this.parentNode.parentNode;
			selector_div.selector.delete();
			selector_div.parentNode.removeChild(selector_div);
		}
	});
}

function draw_selector(selector) {
	function manage_workflow(event) {
		const workflow_id = event ? workflow_select.value : selector.workflowId;

		//reset state ids when workflow is modified
		if(event) {
			selector.stateIds = [];
		}

		if(workflow_id) {
			const values = selector.stateIds || [];
			FormHelpers.FillPalette(states_palette, selected_scope_model.study.getWorkflow(workflow_id).states, undefined, undefined, values);
			states_palette.parentElement.style.display = 'block';
		}
		else {
			states_palette.parentElement.style.display = 'none';
		}
	}

	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('scope_model_workflow_states_selector')).content, true);

	instance.querySelector('div').selector = selector;
	instance.querySelector('button').addEventListener('click', delete_selector);

	const workflow_select = /**@type {HTMLSelectElement}*/ (instance.querySelector('select[data-name="workflowId"]'));
	FormHelpers.FillSelect(workflow_select, selected_scope_model.study.workflows, true);
	workflow_select.value = selector.workflowId || '';
	workflow_select.addEventListener('change', manage_workflow);

	const states_palette = /**@type {AppPalette}*/ (instance.querySelector('app-palette[data-name="stateIds"]'));

	manage_workflow();

	return instance;
}

function manage_parents(event) {
	const parents_ids = event ? this.getValues() : selected_scope_model.parentIds;
	const default_parent_id = /**@type {HTMLInputElement}*/ (document.getElementById('scope_model_default_parent_id'));
	if(!parents_ids.isEmpty()) {
		const parents = parents_ids.map(i => selected_scope_model.study.getScopeModel(i));
		FormHelpers.FillSelect(default_parent_id, parents, true, selected_scope_model.defaultParentId);
		default_parent_id.parentElement.style.display = 'block';
		default_parent_id.setAttribute('required', 'required');
	}
	else {
		//reset and hide fields
		default_parent_id.value = '';
		default_parent_id.parentElement.style.display = 'none';
		default_parent_id.removeAttribute('required');
	}
}

let selected_scope_model;

export default {
	form: 'edit_scope_model_form',
	init: function() {
		document.getElementById('scope_model_parent_ids').addEventListener('change', manage_parents);

		document.getElementById('edit_scope_model_form').addEventListener(
			'submit',
			function(event) {
				event.stop();

				FormHelpers.UpdateObject(selected_scope_model, this);

				document.querySelectorAll('#scope_model_workflow_states_selectors > div').forEach(function(selector_div) {
					const selector = selector_div.selector;
					selector.workflowId = selector_div.querySelector('select[data-name="workflowId"]').value || undefined;
					selector.stateIds = selector_div.querySelector('app-palette[data-name="stateIds"]').getValues();
				});

				FormStaticActions.AfterSubmission(selected_scope_model);
			}
		);

		document.getElementById('scope_model_format_patterns').querySelectorAll('span').forEach(function(pattern) {
			pattern.addEventListener('dragstart', dragstart);
			pattern.addEventListener('dragend', dragend);
		});

		document.getElementById('scope_model_workflow_states_selector_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const selector = new WorkflowStatesSelector();
				selector.parent = selected_scope_model;
				selected_scope_model.workflowStatesSelectors.push(selector);
				document.getElementById('scope_model_workflow_states_selectors').appendChild(draw_selector(selector));
			}
		);

		FormStaticActions.ManageLayoutEdition(document.getElementById('scope_model_layout_add'), document.getElementById('scope_model_layout_edit'), document.getElementById('scope_model_layout_delete'));
	},
	open: function(scope_model) {
		selected_scope_model = scope_model;

		const other_scope_models = scope_model.study.scopeModels.slice();
		other_scope_models.removeElement(scope_model);

		FormHelpers.FillSelect(document.getElementById('scope_model_default_profile_id'), scope_model.study.profiles, true);
		FormHelpers.FillPalette(document.getElementById('scope_model_parent_ids'), other_scope_models);
		FormHelpers.FillPalette(document.getElementById('scope_model_dataset_model_ids'), scope_model.study.datasetModels);
		FormHelpers.FillPalette(document.getElementById('scope_model_form_model_ids'), scope_model.study.formModels);
		FormHelpers.FillPalette(document.getElementById('scope_model_workflow_ids'), scope_model.study.workflows);
		FormHelpers.FillLocalizedInput(document.getElementById('scope_model_shortname'), scope_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('scope_model_longname'), scope_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('scope_model_description'), scope_model.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('scope_model_plural_shortname'), scope_model.study.languages);

		manage_parents();

		FormHelpers.UpdateForm(document.getElementById('edit_scope_model_form'), scope_model);

		scope_model.workflowStatesSelectors.map(draw_selector).forEach(Node.prototype.appendChild, document.getElementById('scope_model_workflow_states_selectors').empty('div'));
		FormStaticActions.UpdateLayoutEdition(scope_model, document.getElementById('scope_model_layout_add'), document.getElementById('scope_model_layout_edit'), document.getElementById('scope_model_layout_delete'));

		FormStaticActions.DrawRules(scope_model, scope_model.createRules, scope_model.constructor.RuleEntities, document.getElementById('scope_model_create_rules'), 'Creation rules');
		FormStaticActions.DrawRules(scope_model, scope_model.removeRules, scope_model.constructor.RuleEntities, document.getElementById('scope_model_remove_rules'), 'Removal rules');
		FormStaticActions.DrawRules(scope_model, scope_model.restoreRules, scope_model.constructor.RuleEntities, document.getElementById('scope_model_restore_rules'), 'Restoration rules');
	}
};
