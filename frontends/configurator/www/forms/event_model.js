import {Config} from '../model_config.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';

function dragstart(event) {
	this.style.opacity = 0.6;
	event.dataTransfer.effectAllowed = 'copy';
	event.dataTransfer.setData('text/plain', this.textContent);
}

function dragend() {
	this.style.opacity = 1;
}

export default {
	form: 'edit_event_model_form',
	init: function() {
		document.getElementById('edit_event_model_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('event_model_planning_reset').addEventListener(
			'click',
			() => {
				/**@type {HTMLInputElement}*/ (document.getElementById('event_model_deadline')).value = '';
				/**@type {HTMLSelectElement}*/ (document.getElementById('event_model_deadline_unit')).value = '';
				/**@type {HTMLSelectElement}*/ (document.getElementById('event_model_deadline_aggregation_function')).value = '';
				/**@type {AppPalette}*/ (document.getElementById('event_model_deadline_reference_event_model_ids')).value = '';
				/**@type {HTMLInputElement}*/ (document.getElementById('event_model_interval')).value = '';
				/**@type {HTMLSelectElement}*/ (document.getElementById('event_model_interval_unit')).value = '';
			}
		);

		document.getElementById('event_model_labels_patterns').querySelectorAll('span').forEach(function(pattern) {
			pattern.addEventListener('dragstart', dragstart);
			pattern.addEventListener('dragend', dragend);
		});

		FormStaticActions.ManageConstraintEdition('constraint', document.getElementById('event_model_constraint_add'), document.getElementById('event_model_constraint_edit'), document.getElementById('event_model_constraint_delete'));
	},
	open: function(event_model) {

		const other_event_models = event_model.scopeModel.eventModels.filter(e => e !== event_model);

		FormHelpers.FillSelectEnum(document.getElementById('event_model_deadline_unit'), Config.Enums.EventTimeUnit, true);
		FormHelpers.FillSelectEnum(document.getElementById('event_model_interval_unit'), Config.Enums.EventTimeUnit, true);
		if(event_model.scopeModel.eventModels.length > 1) {
			document.getElementById('event_model_planning').style.display = 'block';
			FormHelpers.FillSelect(document.getElementById('event_model_deadline_reference_event_model_ids'), other_event_models, true);
		}
		else {
			document.getElementById('event_model_planning').style.display = 'none';
		}
		FormHelpers.FillLocalizedInput(document.getElementById('event_model_shortname'), event_model.scopeModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('event_model_longname'), event_model.scopeModel.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('event_model_description'), event_model.scopeModel.study.languages);
		FormHelpers.FillSelect(document.getElementById('event_model_event_group_id'), event_model.scopeModel.eventGroups, true);
		FormHelpers.FillSelectEnum(document.getElementById('event_model_deadline_aggregation_function'), Config.Enums.DateAggregationFunction);
		FormHelpers.FillPalette(document.getElementById('event_model_dataset_model_ids'), event_model.scopeModel.study.datasetModels);
		FormHelpers.FillPalette(document.getElementById('event_model_form_model_ids'), event_model.scopeModel.study.formModels);
		FormHelpers.FillPalette(document.getElementById('event_model_workflow_ids'), event_model.scopeModel.study.workflows);
		FormHelpers.FillPalette(document.getElementById('event_model_implied_event_model_ids'), other_event_models);
		FormHelpers.FillPalette(document.getElementById('event_model_blocked_event_model_ids'), other_event_models);
		FormHelpers.UpdateForm(document.getElementById('edit_event_model_form'), event_model);

		FormStaticActions.UpdateConstraintEdition(event_model, 'constraint', document.getElementById('event_model_constraint_add'), document.getElementById('event_model_constraint_edit'), document.getElementById('event_model_constraint_delete'));

		FormStaticActions.DrawRules(event_model, event_model.createRules, event_model.constructor.RuleEntities, document.getElementById('event_model_create_rules'), 'Creation rules');
		FormStaticActions.DrawRules(event_model, event_model.removeRules, event_model.constructor.RuleEntities, document.getElementById('event_model_remove_rules'), 'Removal rules');
		FormStaticActions.DrawRules(event_model, event_model.restoreRules, event_model.constructor.RuleEntities, document.getElementById('event_model_restore_rules'), 'Restoration rules');
	}
};
