import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class EventConfigurationHook extends Node {
	static getProperties() {
		return {
			eventGroupId: {type: 'string'},
			eventModelId: {type: 'string'},
			documentModels: {type: 'array'},
			formModels: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.eventGroupId = undefined;
		this.eventModelId = undefined;
		this.documentModels = [];
		this.formModels = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
