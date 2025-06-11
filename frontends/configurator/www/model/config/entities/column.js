import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class Column extends Node {
	static getProperties() {
		return {
			layout: {type: Entities.Layout.name, back_reference: true},
			cssCode: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.layout = undefined;
		this.cssCode = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
	getLocalizedLabel() {
		return this.layout.columns.indexOf(this).toString();
	}
}
