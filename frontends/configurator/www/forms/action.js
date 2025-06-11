import {UI} from '../tools/ui.js';
import {Effects} from '../tools/effects.js';
import {NodeTools} from '../node_tools.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

function manage_documentable() {
	if(/**@type {HTMLInputElement}*/ (document.getElementById('action_documentable')).checked) {
		document.getElementById('action_documentable_options').parentElement.style.display = 'block';
	}
	else {
		//reset and hide field
		document.getElementById('action_documentable_options').empty('div');
		document.getElementById('action_documentable_options').parentElement.style.display = 'none';
	}
}

function manage_require_signature() {
	const action_require_signature_text = /**@type {AppLocalizedInput}*/ (document.getElementById('action_require_signature_text'));
	if(/**@type {HTMLInputElement}*/ (document.getElementById('action_require_signature')).checked) {
		action_require_signature_text.parentElement.style.display = 'block';
	}
	else {
		//reset and hide field
		action_require_signature_text.value = '{}';
		action_require_signature_text.parentElement.style.display = 'none';
	}
}

function delete_option(event) {
	event.stop();
	UI.Validate('Are you sure you want to delete this option?').then(confirmed => {
		if(confirmed) {
			const option_div = this.parentNode;
			option_div.parentNode.removeChild(option_div);
		}
	});
}

function draw_documentable_option(option) {
	const instance = document.importNode(/**@type {HTMLTemplateElement}*/ (document.getElementById('action_documentable_option')).content, true);

	instance.querySelector('button').addEventListener('click', delete_option);
	FormHelpers.FillLocalizedInput(instance.querySelector('app-localized-input[data-name="text"]'), selected_action.workflow.study.languages, option);
	return instance;
}

let selected_action;

export default {
	form: 'edit_action_form',
	init: function() {
		document.getElementById('action_documentable').addEventListener('change', manage_documentable);
		document.getElementById('action_require_signature').addEventListener('change', manage_require_signature);

		document.getElementById('edit_action_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_action, this['id'].value)) {
					FormHelpers.UpdateObject(selected_action, this);
					selected_action.documentableOptions = document.querySelectorAll('#action_documentable_options > p app-localized-input[data-name="text"]').map(i => JSON.parse(i.value));
					FormStaticActions.AfterSubmission(selected_action);
				}
			}
		);

		document.getElementById('action_documentable_options_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				document.getElementById('action_documentable_options').appendChild(draw_documentable_option({}));
			}
		);

		Effects.Sortable(
			document.getElementById('action_documentable_options'),
			undefined,
			'p > img:first-child',
			undefined,
			'p'
		);
	},
	open: function(action) {
		selected_action = action;

		FormHelpers.FillLocalizedInput(document.getElementById('action_shortname'), action.workflow.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('action_longname'), action.workflow.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('action_description'), action.workflow.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('action_require_signature_text'), action.workflow.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_action_form'), action);

		FormStaticActions.DrawRules(action, action.rules, action.constructor.RuleEntities, document.getElementById('action_rules'));

		manage_documentable();
		manage_require_signature();
		NodeTools.DrawUsage(action, document.getElementById('action_usage'));

		action.documentableOptions.map(draw_documentable_option).forEach(Node.prototype.appendChild, document.getElementById('action_documentable_options').empty('p'));
	}
};
