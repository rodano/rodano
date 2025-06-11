import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class PaymentStep extends DisplayableNode {
	static getProperties() {
		return {
			paymentPlan: {type: Entities.PaymentPlan.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			repeatable: {type: 'boolean'},
			workflowable: {type: 'string'},
			distributions: {type: 'array', subtype: Entities.PaymentDistribution.name}
		};
	}

	constructor(values) {
		super();
		this.paymentPlan = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.repeatable = undefined;
		this.workflowable = undefined;
		this.distributions = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getPayableModels() {
		const payable_models = [];
		for(let i = 0; i < this.distributions.length; i++) {
			const distribution_payable_model = this.distributions[i].getPayableModel();
			if(!payable_models.includes(distribution_payable_model)) {
				payable_models.push(distribution_payable_model);
			}
		}
		return payable_models;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.PaymentDistribution:
				return this.distributions.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.PaymentDistribution:
				this.distributions.push(child);
				child.step = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChangeEventModelId(event) {
		if(this.workflowable && this.workflowable === event.oldValue) {
			this.workflowable = event.newValue;
		}
	}
	onDeleteEventModel(event) {
		if(this.workflowable === event.node.id) {
			this.workflowable = undefined;
		}
	}

	onDeletePaymentDistribution(event) {
		this.distributions.removeElement(event.node);
	}
	onMovePaymentDistribution(event) {
		if(event.newParent === this) {
			event.node.step.distributions.removeElement(event.node);
			event.node.step = this;
			this.distributions.push(event.node);
		}
	}
}
