import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class ProfileRight extends Node {
	static getProperties() {
		return {
			system: {type: 'boolean'},
			profileIds: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.system = false;
		this.profileIds = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
