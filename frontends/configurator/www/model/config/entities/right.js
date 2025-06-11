import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class Right extends Node {
	static getProperties() {
		return {
			right: {type: 'boolean'},
			childRights: {type: 'object'}
		};
	}

	constructor(values) {
		super();
		this.right = false;
		this.childRights = {};
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
