import {Entities} from '../entities.js';
import {EntitiesHooks} from '../entities_hooks.js';
import {Node} from '../node.js';

export class SelectionNode extends Node {
	static getProperties() {
		return {
			parent: {back_reference: true},
			nodeEntity: {type: 'string'},
			nodeId: {type: 'string'},
			selections: {type: 'array', subtype: 'SelectionNode'}
		};
	}

	static SELECTABLE_ENTITIES = [Entities.ScopeModel, Entities.EventModel, Entities.FormModel, Entities.Layout];

	constructor(values) {
		super();
		this.parent = undefined;
		this.nodeEntity = undefined;
		this.nodeId = undefined;
		this.selections = [];
		EntitiesHooks?.CreateNode.call(this, values);
	}

	getLocalizedLabel() {
		return 'Selection node';
	}

	//retrieve good entity (e.g. from SCOPE_MODEL to ScopeModel)
	getConfigurationNodeEntity() {
		const entry = Object.entries(Entities).find(e => e[1].configuration_name === this.nodeEntity);
		return Entities[entry[0]];
	}
	getChain() {
		const link = {entity: this.getConfigurationNodeEntity().name, id: this.nodeId};
		if(this.parent.getEntity() === Entities.Study) {
			return [link];
		}
		return [...this.parent.getChain(), link];
	}

	//tree
	getChildren(entity) {
		switch(entity) {
			case Entities.SelectionNode:
				return this.selections.slice();
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	addChild(child) {
		const child_entity = child.getEntity();
		switch(child_entity) {
			case Entities.SelectionNode:
				this.selections.push(child);
				child.parent = this;
				break;
			default:
				throw new Error(`Entity ${child_entity.name} cannot be added as a child of entity ${this.getEntity().name}`);
		}
		EntitiesHooks?.AddChildNode.call(this, child);
	}

	//bus
	onChange(event) {
		const entity = event.node.getEntity();
		//react only to entities that can be selected
		if(SelectionNode.SELECTABLE_ENTITIES.includes(entity)) {
			if(this.nodeId === event.oldValue && this.getConfigurationNodeEntity() === entity) {
				this.nodeId = event.newValue;
			}
		}
	}
	onDelete(event) {
		const entity = event.node.getEntity();
		//react only to entities that can be selected
		if(SelectionNode.SELECTABLE_ENTITIES.includes(entity)) {
			if(this.nodeId === event.node.id && this.getConfigurationNodeEntity() === entity) {
				//for non root selection nodes, check that the parent of the selection node and the parent of the deleted node match
				if(this.parent.constructor === SelectionNode) {
					const parent = event.node.getParent();
					if(parent.id === this.parent.nodeId) {
						this['delete']();
					}
				}
				else {
					this['delete']();
				}
			}
		}
	}
	onDeleteSelectionNode(event) {
		this.selections.removeElement(event.node);
	}
}
