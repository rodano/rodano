import '../../../basic-tools/extension.js';

import {EntitiesHooks} from '../entities_hooks.js';
import {Utils} from '../utils.js';
import {ComparatorUtils} from '../comparator_utils.js';
import {DisplayableNode} from '../node_displayable.js';
import {Entities} from '../entities.js';

export class Menu extends DisplayableNode {
	static getProperties() {
		return {
			study: {type: Entities.Study.name, back_reference: true},
			id: {type: 'string'},
			shortname: {type: 'object'},
			longname: {type: 'object'},
			description: {type: 'object'},
			parent: {type: Entities.Menu.name, back_reference: true},
			orderBy: {type: 'number'},
			public: {type: 'boolean'},
			homePage: {type: 'boolean'},
			layout: {type: 'string'},
			action: {type: 'string'},
			submenus: {type: 'array', subtype: Entities.Menu.name},
		};
	}
	static getOrderComparator() {
		return (m1, m2) => ComparatorUtils.compareFields(m1, m2, ['orderBy', 'id']);
	}

	constructor(values) {
		super();
		this.study = undefined;
		this.id = undefined;
		this.shortname = {};
		this.longname = {};
		this.description = {};
		this.parent = undefined;
		this.orderBy = undefined;
		this.public = undefined;
		this.homePage = undefined;
		this.layout = undefined;
		this.action = undefined;
		this.submenus = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getStudy() {
		let menu = this;
		while(!menu.study) {
			menu = menu.parent;
		}
		return menu.study;
	}
	getSubmenu(submenu_id) {
		return Utils.getObjectById(this.submenus, submenu_id);
	}

	//parent
	//BUG useless as reviver puts menu or study in "parent" and "study" properties
	//may be useful when reviver will be able to set the good parent
	getParent() {
		return Object.isObject(this.parent) ? this.parent : this.study;
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.Menu:
				return this.submenus.slice();
			case Entities.CMSLayout:
				return this.layout ? [this.layout] : [];
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.Menu:
				this.submenus.push(child);
				child.parent = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onMoveMenu(event) {
		if(event.newParent === this) {
			//if the moved menu is a submenu, remove it from its current parent
			if(event.node.parent) {
				event.node.parent.submenus.removeElement(event.node);
			}
			event.node.parent = this;
			this.submenus.push(event.node);
		}
	}
	onDeleteMenu(event) {
		if(this.parent === event.node) {
			this['delete']();
		}
		this.submenus.removeElement(event.node);
	}

	//report
	report(settings) {
		const report = super.report(settings);
		if(!this.submenus.isEmpty()) {
			if(this.layout && !Object.isEmpty(this.layout) || this.action && !Object.isEmpty(this.action)) {
				report.addError(
					`Menu ${this.id} contains submenus but has an action, a layout or parameters`,
					this,
					function() {
						delete this.layout;
						this.action = {};
					},
					'Delete action, layout and parameters'
				);
			}
		}
		return report;
	}
}
