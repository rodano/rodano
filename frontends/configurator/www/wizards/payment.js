import {bus} from '../model/config/entities_hooks.js';
import {StudyHandler} from '../study_handler.js';
import {Router} from '../router.js';
import {FormHelpers} from '../form_helpers.js';
import {Wizards} from '../wizards.js';
import {Entities} from '../model/config/entities.js';
import {CMSLayout} from '../model/config/entities/cms_layout.js';
import {CMSSection} from '../model/config/entities/cms_section.js';
import {CMSWidget} from '../model/config/entities/cms_widget.js';
import {Layout} from '../model/config/entities/layout.js';
import {FormModel} from '../model/config/entities/form_model.js';
import {PaymentDistribution} from '../model/config/entities/payment_distribution.js';
import {PaymentPlan} from '../model/config/entities/payment_plan.js';
import {PaymentStep} from '../model/config/entities/payment_step.js';

let payment_plan;

function draw_profile(profile) {
	const profile_item = document.createFullElement('li');
	const profile_label = document.createFullElement('label');
	profile_label.appendChild(document.createFullElement('input', {type: 'checkbox', value: profile.id}));
	profile_label.appendChild(document.createTextNode(profile.getLocalizedShortname('en')));
	profile_item.appendChild(profile_label);
	return profile_item;
}

Wizards.Register('payment', {
	title: 'New payment',
	description: 'This wizard will help you to create a payment plan.',
	steps: 4,
	mode: Wizards.Mode.ASIDE,
	labels: {
		'4': 'Close'
	},
	init: function() {
		const study = StudyHandler.GetStudy();
		FormHelpers.FillSelect(document.getElementById('wizard_payment_workflow_id'), study.workflows, true);
		study.profiles.map(draw_profile).forEach(Node.prototype.appendChild, document.getElementById('wizard_payment_profiles').empty());
	},
	onStart: function() {
		payment_plan = undefined;
		/**@type {HTMLInputElement}*/ (document.getElementById('wizard_payment_workflow_id')).value = '';
	},
	onCancel: function() {
		if(payment_plan) {
			payment_plan['delete']();
			payment_plan = undefined;
		}
	},
	onValidate: function(step) {
		switch(step) {
			case 1: {
				const wizard_payment_workflow_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_payment_workflow_id')).value;
				if(!wizard_payment_workflow_id) {
					document.getElementById('wizard_error').textContent = 'Workflow is required';
					document.getElementById('wizard_error').style.display = 'block';
					return false;
				}
				break;
			}
			case 2:
				if(!payment_plan.invoicedScopeModel) {
					document.getElementById('wizard_error').textContent = 'Please choose a invoiced scope model and save the payment plan';
					document.getElementById('wizard_error').style.display = 'block';
					return false;
				}
				break;
		}
		return true;
	},
	onNext: function(step) {
		const study = StudyHandler.GetStudy();
		switch(step) {
			case 1: {
				//find suitable payment id
				let node_id = 'NEW_PAYMENT_PLAN';
				let i = 2;
				while(study.getHasChild(Entities.PaymentPlan, undefined, node_id)) {
					node_id = `${node_id}_${i}`;
					i++;
				}
				//create payment
				bus.pause();
				const workflow_id = /**@type {HTMLInputElement}*/ (document.getElementById('wizard_payment_workflow_id')).value;
				payment_plan = new PaymentPlan({id: node_id, workflow: workflow_id});
				payment_plan.shortname['en'] = 'New payment plan';
				payment_plan.study = study;
				study.addChild(payment_plan);
				//create steps and distributions
				study.getWorkflow(workflow_id).getWorkflowables().map(function(workflowable) {
					//create step
					const step = new PaymentStep({id: workflowable.id, workflowable: workflowable.id});
					step.shortname['en'] = workflowable.shortname['en'];
					//create distribution
					const distribution = new PaymentDistribution({
						scopeModelId: study.getRootScopeModel().id,
						profileId: study.profiles[0].id,
						value: 100
					});
					step.addChild(distribution);
					return step;
				}).forEach(PaymentPlan.prototype.addChild, payment_plan);
				bus.resume();
				Router.SelectNode(payment_plan);
				break;
			}
			case 3: {
				//check if there is not already a dataset model with the same id
				const existing_dataset_model = study.datasetModels.find(d => d.id === payment_plan.id);
				if(existing_dataset_model) {
					existing_dataset_model.delete();
				}
				//create dataset model
				const dataset_model = payment_plan.generateDatasetModel();
				dataset_model.study = study;
				study.addChild(dataset_model);

				//check if there is not already a form model with the same id
				const existing_form_model = study.formModels.find(p => p.id === payment_plan.id);
				if(existing_form_model) {
					existing_form_model.delete();
				}
				//create form model
				const form_model = new FormModel({id: payment_plan.id});
				form_model.shortname['en'] = payment_plan.shortname['en'];
				form_model.study = study;
				study.addChild(form_model);
				//create layout
				const layout = new Layout({id: payment_plan.id, type: 'SINGLE', datasetModelId: dataset_model.id});
				form_model.addChild(layout);

				//retrieve scope model
				const scope_model = study.getScopeModel(payment_plan.invoicedScopeModel);

				//add dataset model in event if needed
				if(!scope_model.datasetModelIds.includes(dataset_model.id)) {
					scope_model.datasetModelIds.push(dataset_model.id);
				}
				//add form model in event model if needed
				if(!scope_model.formModelIds.includes(form_model.id)) {
					scope_model.formModelIds.push(form_model.id);
				}

				//create scope model layout if required
				if(!scope_model.layout) {
					scope_model.layout = new CMSLayout({layoutable: scope_model});
				}

				//add configuration section to scope model layout
				const configuration_section = new CMSSection({id: 'PAYMENT_CONFIGURATION'});
				configuration_section.labels['en'] = 'Payment configuration';
				const configuration_widget = new CMSWidget({id: 'PAYMENT_CONFIGURATION', type: 'DOCUMENT'});
				configuration_widget.parameters = {
					scope_model: scope_model.id,
					document: dataset_model.id,
					form: form_model.id
				};
				configuration_section.addChild(configuration_widget);
				scope_model.layout.addChild(configuration_section);
				//add payment section to scope model layout
				const payment_section = new CMSSection({id: 'PAYMENT_MANAGEMENT'});
				payment_section.labels['en'] = 'Payment';
				const payment_widget = new CMSWidget({id: 'PAYMENT_MANAGEMENT', type: 'PAYMENT_MANAGEMENT'});
				payment_widget.parameters = {
					plan: payment_plan.id
				};
				payment_section.addChild(payment_widget);
				scope_model.layout.addChild(payment_section);
				//add payment batch section to scope model layout
				const payment_batch_section = new CMSSection({id: 'PAYMENT_BATCH_MANAGEMENT'});
				payment_batch_section.labels['en'] = 'Payment batchs';
				const payment_batch_widget = new CMSWidget({id: 'PAYMENT_BATCH_MANAGEMENT', type: 'PAYMENT_BATCH_MANAGEMENT'});
				payment_batch_widget.parameters = {
					plan: payment_plan.id
				};
				payment_batch_section.addChild(payment_batch_widget);
				scope_model.layout.addChild(payment_batch_section);
				break;
			}
			case 4: {
				//gives rights
				document.querySelectorAll('#wizard_payment_profiles input').forEach(function(input) {
					if(input.checked) {
						const profile = study.getProfile(input.value);
						profile.assignRightNode(payment_plan, 'READ');
						profile.assignRightNode(payment_plan, 'WRITE');
						const dataset_model = study.getDatasetModel(payment_plan.id);
						profile.assignRightNode(dataset_model, 'READ');
						profile.assignRightNode(dataset_model, 'WRITE');
						const form_model = study.getFormModel(payment_plan.id);
						profile.assignNode(form_model, 'READ');
					}
				});
				break;
			}
		}
	}
});
