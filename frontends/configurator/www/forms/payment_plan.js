import {UI} from '../tools/ui.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {StudyTree} from '../study_tree.js';

function manage_workflow() {
	const workflow_id = document.getElementById('payment_plan_workflow_id').value;
	let states = [];
	if(workflow_id) {
		states = selected_payment_plan.study.getWorkflow(workflow_id).states;
	}
	FormHelpers.FillSelect(document.getElementById('payment_plan_workflow_state_id'), states);
}

let selected_payment_plan;

export default {
	form: 'edit_payment_plan_form',
	init: function() {
		document.getElementById('edit_payment_plan_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		document.getElementById('payment_plan_workflow_id').addEventListener('change', manage_workflow);

		document.getElementById('payment_plan_hook').addEventListener(
			'click',
			function() {
				const event_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById('payment_plan_event_id')).value;
				const event_model = selected_payment_plan.getInvoicedScopeModel().getEventModel(event_model_id);
				const dataset_model = selected_payment_plan.generateDatasetModel();
				dataset_model.study = selected_payment_plan.study;
				//check if there is not already a dataset model with the same id
				const existing_dataset_model_index = selected_payment_plan.study.datasetModels.findIndex(d => d.id === selected_payment_plan.id);
				if(existing_dataset_model_index > -1) {
					selected_payment_plan.study.datasetModels[existing_dataset_model_index] = dataset_model;
				}
				else {
					selected_payment_plan.study.datasetModels.push(dataset_model);
				}
				//add dataset model in event if needed
				if(!event_model.datasetModelIds.includes(selected_payment_plan.id)) {
					event_model.datasetModelIds.push(selected_payment_plan.id);
				}
				//re-draw dataset model entity in the tree
				document.querySelector('ul.dataset_model').parentNode.treeElement.refresh();
				//re-draw dataset model
				StudyTree.GetTree().find(dataset_model).refresh();
				//notify
				UI.Notify(`Dataset model ${dataset_model.id} has been generated and added to event ${event_model.id}`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
		);
	},
	open: function(payment_plan) {
		selected_payment_plan = payment_plan;

		FormHelpers.FillSelect(document.getElementById('payment_plan_invoiced_scope_model_id'), payment_plan.study.scopeModels, false);
		FormHelpers.FillSelect(document.getElementById('payment_plan_workflow_id'), payment_plan.study.workflows, false, payment_plan.workflow);
		FormHelpers.FillSelect(document.getElementById('payment_plan_event_id'), payment_plan.study.getEventModels());
		FormHelpers.FillLocalizedInput(document.getElementById('payment_plan_shortname'), payment_plan.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('payment_plan_longname'), payment_plan.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('payment_plan_description'), payment_plan.study.languages);

		//workflow id
		manage_workflow();

		FormHelpers.UpdateForm(document.getElementById('edit_payment_plan_form'), payment_plan);
	}
};
