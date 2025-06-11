import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class ScopeCriterionRight extends Node {
	static getProperties() {
		return {
			rightEntity: {type: 'string'},
			right: {type: 'string'},
			id: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.rightEntity = undefined;
		this.right = undefined;
		this.id = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeScopeModelId(event) {
		if(this.rightEntity === Entities.ScopeModel.configuration_name && this.id && this.id === event.oldValue) {
			this.id = event.newValue;
		}
	}
	onDeleteScopeModel(event) {
		if(this.rightEntity === Entities.ScopeModel.configuration_name && this.id === event.node.id) {
			this.rightEntity = undefined;
			this.right = undefined;
			this.id = undefined;
		}
	}

	onChangeEventModelId(event) {
		if(this.rightEntity === Entities.EventModel.configuration_name && this.id && this.id === event.oldValue) {
			this.id = event.newValue;
		}
	}
	onDeleteEventModel(event) {
		if(this.rightEntity === Entities.EventModel.configuration_name && this.id === event.node.id) {
			this.rightEntity = undefined;
			this.right = undefined;
			this.id = undefined;
		}
	}

	onChangeDatasetModelId(event) {
		if(this.rightEntity === Entities.DatasetModel.configuration_name && this.id && this.id === event.oldValue) {
			this.id = event.newValue;
		}
	}
	onDeleteDatasetModel(event) {
		if(this.rightEntity === Entities.DatasetModel.configuration_name && this.id === event.node.id) {
			this.rightEntity = undefined;
			this.right = undefined;
			this.id = undefined;
		}
	}

	onChangeProfileId(event) {
		if(this.rightEntity === Entities.Profile.configuration_name && this.id && this.id === event.oldValue) {
			this.id = event.newValue;
		}
	}
	onDeleteProfile(event) {
		if(this.rightEntity === Entities.Profile.configuration_name && this.id === event.node.id) {
			this.rightEntity = undefined;
			this.right = undefined;
			this.id = undefined;
		}
	}

	onChangePaymentPlan(event) {
		if(this.rightEntity === Entities.PaymentPlan.configuration_name && this.id && this.id === event.oldValue) {
			this.id = event.newValue;
		}
	}
	onDeletePaymentPlan(event) {
		if(this.rightEntity === Entities.PaymentPlan.configuration_name && this.id === event.node.id) {
			this.rightEntity = undefined;
			this.right = undefined;
			this.id = undefined;
		}
	}
}
