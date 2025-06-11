import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class PossibleValue extends Node {
	static getProperties() {
		return {
			fieldModel: {type: Entities.FieldModel.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			exportLabel: {type: 'string'},
			specify: {type: 'boolean'},
		};
	}

	constructor(values) {
		super();
		this.fieldModel = undefined;
		this.id = undefined;
		this.shortname = {};
		this.exportLabel = undefined;
		this.specify = false;
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//shortname
	getLocalizedShortname(languages) {
		return Utils.getLocalizedField.call(this, 'shortname', languages);
	}
	getLocalizedLabel(languages) {
		return this.getLocalizedShortname(languages) || this.id;
	}
}
