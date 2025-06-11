import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class CMSAction extends Node {
	static getProperties() {
		return {
			id: {type: 'string'},
			labels: {type: 'object'},
			page: {type: 'string'},
			context: {type: 'array'},
			parameters: {type: 'object'},
			section: {type: 'string'},
		};
	}

	constructor(values) {
		super();
		this.id = undefined;
		this.labels = {};
		this.page = undefined;
		this.context = [];
		this.parameters = {};
		this.section = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
