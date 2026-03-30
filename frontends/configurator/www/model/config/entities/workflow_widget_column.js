import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class WorkflowWidgetColumn extends Node {
	static getProperties() {
		return {
			widget: {type: Entities.WorkflowWidget.name, back_reference: true},
			label: {type: 'object'},
			type: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.widget = undefined;
		this.label = {};
		this.type = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'label', languages);
	}
}
