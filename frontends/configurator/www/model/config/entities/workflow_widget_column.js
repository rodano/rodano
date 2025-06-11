import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class WorkflowWidgetColumn extends Node {
	static getProperties() {
		return {
			widget: {type: Entities.WorkflowWidget.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			type: {type: 'string'},
			width: {type: 'number'}
		};
	}

	constructor(values) {
		super();
		this.widget = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.type = undefined;
		this.width = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
