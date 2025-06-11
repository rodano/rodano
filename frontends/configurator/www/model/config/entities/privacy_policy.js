import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {DisplayableNode} from '../node_displayable.js';

export class PrivacyPolicy extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			content: {type: 'object'},
			profileIds: {type: 'array'}
		};
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.content = {};
		this.profileIds = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	//bus
	onChangeProfileId(event) {
		if(this.profileId && this.profileId === event.oldValue) {
			this.profileId = event.newValue;
		}
	}
	onDeleteProfile(event) {
		if(this.profileId === event.node.id) {
			this.profileId = undefined;
		}
	}
}
