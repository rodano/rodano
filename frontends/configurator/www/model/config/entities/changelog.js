import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class Changelog extends Node {
	static getProperties() {
		return {
			date: {type: 'string'},
			user: {type: 'string'},
			message: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.date = undefined;
		this.user = undefined;
		this.message = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
