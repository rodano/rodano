import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';
import {Utils} from '../utils.js';
import {FieldModel} from './field_model.js';
import {DatasetModel} from './dataset_model.js';
import {PossibleValue} from './possible_value.js';

export class PaymentPlan extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			currency: {type: 'string'},
			invoicedScopeModel: {type: 'string'},
			workflow: {type: 'string'},
			templateFile: {type: 'string'},
			state: {type: 'string'},
			allowBatchMerger: {type: 'boolean'},
			steps: {type: 'array', subtype: Entities.PaymentStep.name},
			extendedSteps: {type: 'boolean'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.currency = undefined;
		this.invoicedScopeModel = undefined;
		this.workflow = undefined;
		this.templateFile = undefined;
		this.state = undefined;
		this.allowBatchMerger = undefined;
		this.steps = [];
		this.extendedSteps = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getInvoicedScopeModel() {
		return this.study.getScopeModel(this.invoicedScopeModel);
	}
	getWorkflow() {
		return Utils.getObjectById(this.study.workflows, this.workflow);
	}
	getPayableModels() {
		const payable_models = [];
		for(let i = 0; i < this.steps.length; i++) {
			const step_payable_models = this.steps[i].getPayableModels();
			for(let j = 0; j < step_payable_models.length; j++) {
				if(!payable_models.includes(step_payable_models[j])) {
					payable_models.push(step_payable_models[j]);
				}
			}
		}
		return payable_models;
	}
	generateDatasetModel() {
		const default_language_id = this.study.defaultLanguageId;
		const dataset_model = new DatasetModel({
			id: this.id,
			shortname: this.shortname ? Object.clone(this.shortname): {},
			longname: this.longname ? Object.clone(this.longname) : {}
		});

		function create_field_model(dataset_model, payable_model, id, type, size, default_language_shortname) {
			const shortname = {};
			shortname[default_language_id] = `${default_language_shortname} ${payable_model.getLocalizedShortname(default_language_id)}`;
			const field_model = new FieldModel({
				id: `${id}_${payable_model.id.capitalize()}`,
				type: type,
				size: size,
				shortname: shortname,
				datasetModel: dataset_model
			});
			dataset_model.fieldModels.push(field_model);
		}

		function create_possible_value(field_model, id) {
			const possible_value = new PossibleValue({
				id: id,
				fieldModel: field_model
			});
			field_model.possibleValues.push(possible_value);
		}

		this.getPayableModels().forEach(payable_model => {
			create_field_model(dataset_model, payable_model, 'IBAN', 'STRING', 25, 'IBAN');
			create_field_model(dataset_model, payable_model, 'SWIFT', 'STRING', 15, 'SWIFT');
			create_field_model(dataset_model, payable_model, 'ACCOUNT_NO', 'STRING', 15, 'Account No.');
			create_field_model(dataset_model, payable_model, 'SPEC_INSTR', 'TEXTAREA', undefined, 'Special instruction');
			create_field_model(dataset_model, payable_model, 'BIC', 'STRING', 15, 'BIC');
		});

		const currency = new FieldModel({
			id: 'CURRENCY',
			type: 'SELECT',
			shortname: {
				en: 'Currency',
				fr: 'Monnaie'
			},
			valueFormula: this.currency,
			datasetModel: dataset_model
		});
		create_possible_value(currency, 'EUR');
		create_possible_value(currency, 'USD');
		create_possible_value(currency, 'AUD');
		create_possible_value(currency, 'CAD');
		create_possible_value(currency, 'CHF');
		create_possible_value(currency, 'GBP');
		create_possible_value(currency, 'JPY');
		create_possible_value(currency, 'NOK');
		create_possible_value(currency, 'NZD');
		create_possible_value(currency, 'ZAR');

		dataset_model.fieldModels.push(currency);

		const vat = new FieldModel({
			id: 'VAT',
			type: 'NUMBER',
			shortname: {
				en: 'VAT',
				fr: 'TVA'
			},
			format: '0.00',
			valueFormula: '0.00',
			datasetModel: dataset_model
		});
		dataset_model.fieldModels.push(vat);

		this.steps.forEach(step => {
			step.distributions.forEach(distribution => {
				const shortname = {};
				shortname[default_language_id] = `${step.getLocalizedShortname(default_language_id)} ${distribution.getPayableModel().getLocalizedShortname(default_language_id)}`;
				const field_model = new FieldModel({
					id: `${step.id.capitalize()}_${distribution.getPayableModelId().capitalize()}`,
					shortname: shortname,
					type: 'NUMBER',
					format: '#',
					valueFormula: `${distribution.value}`,
					size: 7,
					maxLength: 10,
					validatorIds: ['REQUIRED'],
					datasetModel: dataset_model
				});
				dataset_model.fieldModels.push(field_model);
			});
		});

		return dataset_model;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.PaymentStep:
				return this.steps.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.PaymentStep:
				this.steps.push(child);
				child.paymentPlan = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChangeWorkflowId(event) {
		if(this.workflow && this.workflow === event.oldValue) {
			this.workflow = event.newValue;
		}
	}
	onDeleteWorkflow(event) {
		if(this.workflow === event.node.id) {
			this.workflow = undefined;
		}
	}

	onChangeWorkflowStateId(event) {
		if(this.state && this.state === event.oldValue) {
			this.state = event.newValue;
		}
	}
	onDeleteWorkflowState(event) {
		if(this.state === event.node.id) {
			this.state = undefined;
		}
	}

	onDeletePaymentStep(event) {
		this.steps.removeElement(event.node);
	}
	onMovePaymentStep(event) {
		if(event.newParent === this) {
			event.node.paymentPlan.steps.removeElement(event.node);
			event.node.paymentPlan = this;
			this.steps.push(event.node);
		}
	}
}
