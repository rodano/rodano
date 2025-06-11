import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class Feature extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			staticNode: {type: 'boolean'},
			optional: {type: 'boolean'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.staticNode = undefined;
		this.optional = false;
		EntitiesHooks?.CreateNode.call(this, values);
	}
}
