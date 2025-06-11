import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class ResourceCategory extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			color: {type: 'string'},
			icon: {type: 'string'},
			staticNode: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.color = undefined;
		this.icon = undefined;
		this.staticNode = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
