import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class PaymentDistribution extends Node {
	static getProperties() {
		return {
			step: {type: Entities.PaymentStep.name, back_reference: true},
			scopeModelId: {type: 'string'},
			profileId: {type: 'string'},
			value: {type: 'number'}
		};
	}

	constructor(values) {
		super();
		this.step = undefined;
		this.scopeModelId = undefined;
		this.profileId = undefined;
		this.value = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getPayableModelId() {
		return this.profileId ? this.profileId : this.scopeModelId;
	}
	getPayableModel() {
		return this.profileId ? this.step.paymentPlan.study.getProfile(this.profileId) : this.step.paymentPlan.study.getScopeModel(this.scopeModelId);
	}

	//bus
	onChangeScopeModelId(event) {
		if(this.scopeModelId && this.scopeModelId === event.oldValue) {
			this.scopeModelId = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.scopeModelId === event.node.id) {
			this.scopeModelId = undefined;
		}
	}

	onChangeProfileId(event) {
		if(this.profileId && this.profileId === event.oldValue) {
			this.profileId = event.newValue;
		}
	}
	onDeleteProfile(event) {
		if(this.profileId === event.node.id) {
			this.profileId = undefined;
		}
	}
}
