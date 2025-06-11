import {DisplayableNode} from '../node_displayable.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Entities} from '../entities.js';

export class EventGroup extends DisplayableNode {
	static getProperties() {
		return {
			scopeModel: {type: Entities.ScopeModel.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			orderBy: {type: 'string'},
			icon: {type: 'string'}
		};
	}

	constructor(values) {
		super();
		this.scopeModel = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.orderBy = undefined;
		this.icon = undefined;
		EntitiesHooks?.CreateNode.call(this, values);
	}
	getEventModels() {
		return this.scopeModel.eventModels.filter(e => e.eventGroupId && e.eventGroupId === this.id);
	}

	//tree
	getRelations(entity) {
		switch(entity) {
			case Entities.EventModel:
				return this.getEventModels();
		}
		throw new Error(`Entity ${entity.name} is not related to entity ${this.getEntity().name}`);
	}
}
