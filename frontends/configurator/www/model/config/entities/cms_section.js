import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';
import {Utils} from '../utils.js';

export class CMSSection extends Node {
	static getProperties() {
		return {
			layout: {type: Entities.CMSLayout.name, back_reference: true},
			id: {type: 'string'},
			labels: {type: 'object'},
			requiredFeature: {type: 'string'},
			requiredRight: {type: Entities.ScopeCriterionRight.name},
			widgets: {type: 'array', subtype: Entities.CMSWidget.name}
		};
	}

	constructor(values) {
		super();
		this.layout = undefined;
		this.id = undefined;
		this.labels = {};
		this.requiredFeature = undefined;
		this.requiredRight = undefined;
		this.widgets = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}
	getLocalizedLabel(languages) {
		return Utils.getLocalizedField.call(this, 'labels', languages) || this.id;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.CMSWidget:
				return this.widgets.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.CMSWidget:
				this.widgets.push(child);
				child.layout = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onDeleteCMSWidget(event) {
		this.widgets.removeElement(event.node);
	}
	onMoveCMSWidget(event) {
		if(event.newParent === this) {
			event.node.section.widgets.removeElement(event.node);
			event.node.section = this;
			this.widgets.push(event.node);
		}
	}
}
