import {UI} from '../tools/ui.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {PaymentDistribution} from '../model/config/entities/payment_distribution.js';

function draw_distribution(distribution, index) {
	const distribution_div = document.createElement('div');
	distribution_div.distribution = distribution;

	const distribution_title = document.createFullElement('h3', {}, `Distribution n°${index + 1}`);
	const delete_distribution_button = document.createFullElement('img', {src: 'images/cross.png', alt: 'Delete', style: 'cursor: pointer; vertical-align: text-bottom; margin-left: 1rem;'});
	delete_distribution_button.addEventListener(
		'click',
		function(event) {
			event.stop();
			UI.Validate('Are you sure you want to delete this distribution?').then(confirmed => {
				if(confirmed) {
					distribution.delete();
					const distribution_div = this.parentNode.parentNode;
					distribution_div.parentNode.removeChild(distribution_div);
				}
			});
		}
	);
	distribution_title.appendChild(delete_distribution_button);
	distribution_div.appendChild(distribution_title);

	let paragraph = document.createFullElement('p');
	distribution_div.appendChild(paragraph);
	paragraph.appendChild(document.createFullElement('label', {'for': `distribution_scope_model_id_${index}`}, 'Scope model'));
	const distribution_scope_model_id = document.createFullElement('select', {id: `distribution_scope_model_id_${index}`});
	FormHelpers.FillSelect(distribution_scope_model_id, distribution.step.paymentPlan.study.scopeModels, true, distribution.scopeModelId);
	paragraph.appendChild(distribution_scope_model_id);

	paragraph = document.createFullElement('p');
	distribution_div.appendChild(paragraph);
	paragraph.appendChild(document.createFullElement('label', {'for': `distribution_profile_id_${index}`}, 'Profile'));
	const distribution_profile_id = document.createFullElement('select', {id: `distribution_profile_id_${index}`});
	FormHelpers.FillSelect(distribution_profile_id, distribution.step.paymentPlan.study.profiles, true, distribution.profileId);
	paragraph.appendChild(distribution_profile_id);

	paragraph = document.createFullElement('p');
	distribution_div.appendChild(paragraph);
	paragraph.appendChild(document.createFullElement('label', {'for': `distribution_value_${index}`}, 'Default value'));
	paragraph.appendChild(document.createFullElement('input', {id: `distribution_value_${index}`, value: distribution.value ? distribution.value : '', type: 'number', style: 'margin-bottom: 12px;'}));

	return distribution_div;
}

let selected_payment_step;

export default {
	form: 'edit_payment_step_form',
	init: function() {
		document.getElementById('edit_payment_step_form').addEventListener(
			'submit',
			function(event) {
				event.stop();
				if(FormStaticActions.CheckId(selected_payment_step, this['id'].value)) {
					FormHelpers.UpdateObject(selected_payment_step, this);

					//update distribution
					const distribution_divs = document.querySelectorAll('#payment_distributions > div');
					for(let i = 0; i < distribution_divs.length; i++) {
						//retrieve ui id
						const distribution_div = distribution_divs[i];
						const distribution_ui_id = distribution_div.querySelector('input').id.substring(distribution_div.querySelector('input').id.lastIndexOf('_') + 1);
						const distribution = distribution_div.distribution;
						const distribution_scope_model_id = /**@type {HTMLSelectElement}*/ (document.getElementById(`distribution_scope_model_id_${distribution_ui_id}`));
						distribution.scopeModelId = distribution_scope_model_id.value || undefined;
						const distribution_profile_id = /**@type {HTMLSelectElement}*/ (document.getElementById(`distribution_profile_id_${distribution_ui_id}`));
						distribution.profileId = distribution_profile_id.value || undefined;
						const distribution_value = /**@type {HTMLInputElement}*/ (document.getElementById(`distribution_value_${distribution_ui_id}`));
						distribution.value = distribution_value.value ? parseInt(distribution_value.value) : undefined;
					}

					FormStaticActions.AfterSubmission(selected_payment_step);
				}
			}
		);

		document.getElementById('payment_step_distribution_add').addEventListener(
			'click',
			function(event) {
				event.stop();
				const distribution = new PaymentDistribution();
				distribution.step = selected_payment_step;
				selected_payment_step.distributions.push(distribution);
				document.getElementById('payment_distributions').appendChild(draw_distribution(distribution, selected_payment_step.distributions.length - 1));
			}
		);
	},
	open: function(payment_step) {
		selected_payment_step = payment_step;

		FormHelpers.FillSelect(document.getElementById('payment_step_workflowable'), payment_step.paymentPlan.getWorkflow().getWorkflowables(), true);
		FormHelpers.FillLocalizedInput(document.getElementById('payment_step_shortname'), payment_step.paymentPlan.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('payment_step_longname'), payment_step.paymentPlan.study.languages);
		FormHelpers.FillLocalizedInput(document.getElementById('payment_step_description'), payment_step.paymentPlan.study.languages);
		FormHelpers.UpdateForm(document.getElementById('edit_payment_step_form'), payment_step);

		payment_step.distributions.map(draw_distribution).forEach(Node.prototype.appendChild, document.getElementById('payment_distributions').empty());
	}
};
