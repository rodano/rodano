import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class CMSLayout extends Node {
	static getProperties() {
		return {
			layoutable: {back_reference: true}, //do not specify type because layoutable can be a menu or a scope model
			sections: {type: 'array', subtype: Entities.CMSSection.name}
		};
	}

	constructor(values) {
		super();
		this.layoutable = undefined;
		this.sections = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel(languages) {
		return `${this.layoutable.getLocalizedLabel(languages)} - Layout`;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.CMSSection:
				return this.sections.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.CMSSection:
				this.sections.push(child);
				child.layout = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onDeleteCMSSection(event) {
		this.sections.removeElement(event.node);
	}
}
