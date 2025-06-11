import '../../basic-tools/extension.js';

import {ComparatorUtils} from './comparator_utils.js';
import {DifferenceArrayElement, DifferenceArrayLength, DifferenceArrayOrdering, DifferenceChild, DifferenceProperty} from './compare.js';
import {Entities} from './entities.js';
import {EntitiesHooks} from './entities_hooks.js';
import {DisplayableNode} from './node_displayable.js';

export class Node {
	static getBackReferences(constructor) {
		return Object.entries(constructor.getProperties())
			.filter(e => e[1].back_reference)
			.map(e => e[0]);
	}
	static getComparator(languages) {
		return (node_1, node_2) => {
			//comparator must be consistent with label used in the tree
			//try to sort on label
			const label_1 = node_1.getLocalizedLabel(languages);
			const label_2 = node_2.getLocalizedLabel(languages);
			if(label_1 !== label_2) {
				if(label_1 && label_2) {
					return label_1.compareTo(label_2);
				}
				return label_1 ? -1 : 1;
			}
			/*
			//try to sort on longname
			var longname_1 = node_1.getLocalizedLongname(languages);
			var longname_2 = node_2.getLocalizedLongname(languages);
			if(longname_1 !== longname_2) {
				if(longname_1 && longname_2) {
					return longname_1.compareTo(longname_2);
				}
				return longname_1 ? -1 : 1;
			}
			//try to sort on shortname
			var shortname_1 = node_1.getLocalizedShortname(languages);
			var shortname_2 = node_2.getLocalizedShortname(languages);
			if(shortname_1 !== shortname_2) {
				if(shortname_1 && shortname_2) {
					return shortname_1.compareTo(shortname_2);
				}
				return shortname_1 ? -1 : 1;
			}
			*/
			//sort on id
			return ComparatorUtils.compareField(node_1, node_2, 'id');
		};
	}

	getEntity() {
		return Entities[this.constructor.name];
	}

	delete() {
		EntitiesHooks?.DeleteNode.call(this);
	}

	//prototype children methods for entities with children

	//get all children for a given entity even if there is multiple links between node and entity
	getAllChildren(entity) {
		if(this.getEntity().children.hasOwnProperty(entity.name)) {
			const size = this.getEntity().children[entity.name].size;
			//multiple children
			return [...Array(size).keys()].flatMap((_, i) => this.getChildren(entity, i));
		}
		throw new Error(`Entity ${entity.name} is not a child of entity ${this.getEntity().name}`);
	}
	//get children for a given entity
	/**
	 * @param {import('./entities.js').Entity} entity - Entity
	 * @param {number} [index] - The index of the relation for entities having more than one relation with specified entity
	 * @returns {Node[]} - A list of child nodes of this node for the specified entity
	 * @abstract
	 */
	getChildren(entity, index) {
		throw new Error(`getChildren(${entity.name}, ${index}) is not implemented for ${this.getEntity().name}`);
	}
	//check if a child with id exists for a given entity
	getHasChild(entity, index, child_id) {
		//console.log('Warning: using generic method to retrieve children');
		const children = this.getChildren(entity, index);
		//use child id as an index for node without id
		if(Number.isNumber(child_id)) {
			return child_id < children.length;
		}
		else {
			return children.some(c => c.id === child_id);
		}
	}
	//get child with id for a given entity
	getChild(entity, index, child_id) {
		//console.log('Warning: using generic method to retrieve children');
		const children = this.getChildren(entity, index);
		//use child id as an index for node without id because normal ids cannot be numbers
		if(Number.isNumber(child_id)) {
			return children[child_id];
		}
		else {
			const child = children.find(c => c.id === child_id);
			if(child) {
				return child;
			}
			throw new Error(`No ${entity.name} with id ${child_id} in ${this.getEntity().name}`);
		}
	}
	//add child for a given entity
	addChild(child, index) {
		throw new Error(`addChild(${child.getEntity().name}, ${index}) is not implemented for ${this.getEntity().name}`);
	}
	//get descendants for a given entity
	getDescendants(entity) {
		const path = this.getEntity().getPath(entity);
		if(path.isEmpty()) {
			throw new Error(`Entity ${entity.name} is not a descendant of entity ${this.getEntity().name}`);
		}
		let results = [this], new_results;
		while(!path.isEmpty()) {
			new_results = [];
			const link = path.shift();
			for(let i = 0; i < results.length; i++) {
				new_results.pushAll(results[i].getAllChildren(link));
			}
			results = new_results;
		}
		return new_results;
	}

	//prototype parent methods for entities with a parent

	//get parent
	hasParent() {
		//const node_entity = this.getEntity();
		//return Object.values(Entities).some(e => e.children.hasOwnProperty(node_entity.name));
		return !Node.getBackReferences(this.constructor).isEmpty();
	}
	getParent() {
		const parent_properties = Node.getBackReferences(this.constructor);
		if(parent_properties.isEmpty()) {
			throw new Error(`No parent for entity ${this.getEntity().name}`);
		}
		//BUG wrong wrong wrong! back reference properties may not match parents relationships, but it works for all entities now
		const parent_property = parent_properties[0];
		//retrieve parent value
		const parent = this[parent_property];
		//check parent property has been set
		if(parent === undefined) {
			throw new Error('Parent has not been set yet');
		}
		return parent;
	}
	//get ancestor for an entity
	getAncestor(entity) {
		if(this.constructor === entity) {
			return this;
		}
		const parent = this.getParent();
		if(parent?.getAncestor) {
			return parent.getAncestor(entity);
		}
		throw new Error(`Entity ${this.getEntity().name} has no ancestor of type ${entity.name}`);
	}
	//get siblings
	getSiblings() {
		if(!this.hasParent()) {
			//no siblings for node without parent
			return [];
		}
		const parent = this.getParent();
		const child_entity = this.getEntity();
		//it may exist one or more properties containing children of this type
		const relation = parent.getEntity().children[child_entity.name];
		let index = 0;
		//if more than one properties contain children of this type, the good one must be found
		if(relation.size > 1) {
			//find in which properties of the parent is the children
			index = Array(relation.size).findIndex((_, i) => parent.getHasChild(child_entity, i, this.id));
		}
		const children = parent.getChildren(child_entity, index).slice();
		children.removeElement(this);
		return children;
	}

	//prototype family methods Object.values(Config.Entities), prototypes
	isDescendantOf(ancestor) {
		//check if entity has parent
		if(this.hasParent()) {
			const parent = this.getParent();
			return parent === ancestor || parent.isDescendantOf(ancestor);
		}
		else {
			return false;
		}
	}
	isAncestorOf(descendant) {
		return descendant.isDescendantOf(this);
	}

	//prototype relations methods for entities with relations

	//get relations for a given entity
	/**
	 * @param {import('./entities.js').Entity} entity - Entity
	 * @returns {Node[]} - A list of nodes related to this node for the specified entity
	 * @abstract
	 */
	getRelations(entity) {
		throw new Error(`getRelations(${entity.name}) not implemented for ${this.getEntity().name}`);
	}
	//get node usage
	getUsage() {
		const relations = {};
		for(const [entity_name, relation] of Object.entries(this.getEntity().relations)) {
			if(relation.structuring) {
				const entity = Entities[entity_name];
				const entity_relations = this.getRelations(entity);
				if(!entity_relations.isEmpty()) {
					relations[entity_name] = entity_relations;
				}
			}
		}
		return relations;
	}
	//check node usage
	isUsed() {
		for(const [entity_name, relation] of Object.entries(this.getEntity().relations)) {
			if(relation.structuring) {
				const entity = Entities[entity_name];
				if(!this.getRelations(entity).isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	//prototype identification, search and label management methods

	//get global id for all nodes
	getGlobalId(reference) {
		let id = '';
		let node = this;
		let parent = this;
		do {
			node = parent;
			parent = undefined;
			//first thing to do is to add the node entity to the id
			const node_entity = node.getEntity();
			let node_id = node_entity.name;
			//find if node "can" have a parent
			if(this.hasParent()) {
				try {
					//find parent
					parent = node.getParent();
					if(parent) {
						const relation = parent.getEntity().children[node_entity.name];
						//check if parent has more than one child property for node entity
						if(relation.size > 1) {
							let property_index = 0;
							let node_index = 0;
							while(property_index <= relation.size) {
								node_index = parent.getChildren(node_entity, property_index).indexOf(node);
								if(node_index > -1) {
									break;
								}
								property_index++;
							}
							//add child property index to node id
							node_id += (`-${property_index}:`);
							//identify node with its id or its index
							node_id += node.id || node_index;
						}
						else {
							//identify node with its id or its index
							node_id += (`:${node.id || parent.getChildren(node_entity).indexOf(node)}`);
						}
					}
				}
				catch {
					//parent has not been set yet
					//the only thing to do is to try to build an incomplete id based on node id
					//this will fail if node does not have an id
					node_id += node.id ? `:${node.id}` : '';
					//keep parent undefined to stop the loop
				}
			}
			else {
				node_id += (`:${node.id}`);
				//keep parent undefined to stop the loop
			}
			//add separator if needed
			if(id) {
				id = `|${id}`;
			}
			//add current node id to global id
			id = node_id + id;
		}
		//stop loop if there is no more node to work with (if reference node or root node have been reached, or if there is no parent because root node has been reached or parent has not been set yet)
		while(parent && node !== reference);
		//reference must have been "reached" at this point
		if(reference && node !== reference) {
			throw new Error(`${this.getEntity().name} ${this.id || ''} is not related to ${reference.getEntity().name} ${reference.id || ''}`);
		}
		return id;
	}

	//get node id for all nodes
	getNode(global_id) {
		if(!global_id) {
			throw new Error('Global id cannot be blank');
		}
		const entity = this.getEntity();
		const local_ids = global_id.split('|');
		//retrieve and remove first part because it represents current node (this)
		const self = local_ids.shift();
		const self_parts = self.split(':');
		//check that the self variable is really equal to this
		//this is a way to check that a real global id has been provided, not just junk
		//this may happen during a copy/paste when we try to retrieve a node from content of the clipboard which may be something completely different
		if(self_parts[0] !== entity.name || self_parts[1] !== this.id) {
			throw new Error(`"${global_id}" is not a valid global id`);
		}
		let node = /**@type {Node}*/ (this);
		while(!local_ids.isEmpty()) {
			const local_id = local_ids.shift().split(':');
			let entity_name = local_id[0], index;
			if(entity_name.includes('-')) {
				index = parseInt(entity_name.substring(entity_name.indexOf('-') + 1));
				entity_name = entity_name.substring(0, entity_name.indexOf('-'));
			}
			try {
				const entity = Entities[entity_name];
				node = node.getChild(entity, index, local_id[1]);
			}
			catch(exception) {
				throw new Error(`No node matching id ${global_id} in ${entity.name} ${this.id || ''}: ${exception.message}`);
			}
		}
		return node;
	}
	//search node
	search(search, languages) {
		const filter = search.toLowerCase();
		const results = [];
		for(const [entity_name, relation] of Object.entries(this.getEntity().children)) {
			for(let i = 0; i < relation.size; i++) {
				const entity = Entities[entity_name];
				const children = this.getChildren(entity, i);
				for(let j = 0; j < children.length; j++) {
					const child = children[j];
					if(child.id?.toLowerCase().includes(filter)) {
						results.push(child);
					}
					else if(child instanceof DisplayableNode) {
						const child_label = child.getLocalizedLabel(languages);
						if(child_label?.toLowerCase().includes(filter)) {
							results.push(child);
						}
					}
					results.pushAll(child.search(filter, languages));
				}
			}
		}
		return results;
	}
	/**
	 * @param {Node} other_node - The other node to compare this node with
	 * @returns {(DifferenceArrayLength|DifferenceArrayOrdering|DifferenceArrayElement|DifferenceProperty|DifferenceChild)[]} - The list of differences between the two nodes
	 */
	compareSelf(other_node) {
		const differences = [];
		//compare properties
		const properties = this.constructor.getProperties();
		//find properties used as links and references that will not be compared
		const link_properties = Object.entries(properties).filter(e => Entities.hasOwnProperty(e[1].type) || Entities.hasOwnProperty(e[1].subtype)).map(e => e[0]);
		link_properties.pushAll(Node.getBackReferences(this.constructor));
		for(const property_id in properties) {
			if(properties.hasOwnProperty(property_id)) {
				//do not do comparison on properties which are links
				if(!link_properties.includes(property_id)) {
					const value = this[property_id];
					const other_value = other_node[property_id];
					//compare only non objects
					if(!Object.isObject(value)) {
						//arrays
						if(Array.isArray(value)) {
							if(other_value === undefined) {
								differences.push(new DifferenceProperty(this, property_id, value, other_value));
							}
							else {
								const additions = {};
								const deletions = {};
								const element_positions = [];
								for(let i = 0; i < value.length; i++) {
									const element = value[i];
									//TODO use indexOf as these arrays should only contains primitive types
									const index = other_value.indexOfSame(element);
									if(index === -1) {
										//element has been added
										additions[i] = element;
									}
									else {
										//track element index in the other array
										element_positions.push(index);
									}
								}
								//look for deleted elements
								if(element_positions.length < other_value.length) {
									for(let i = 0; i < other_value.length; i++) {
										if(!element_positions.includes(i)) {
											deletions[i] = other_value[i];
										}
									}
								}
								//keep a hook on added and deleted elements
								const elements_modified_added = Object.values(additions);
								const elements_modified_deleted = Object.values(deletions);
								//record additions and deletions that occur at the same index as modifications
								for(const i in additions) {
									if(additions.hasOwnProperty(i) && deletions.hasOwnProperty(i)) {
										differences.push(new DifferenceArrayElement(this, property_id, i, additions[i], deletions[i]));
										delete additions[i];
										delete deletions[i];
									}
								}
								//retrieve really added and really deleted elements
								const elements_added = Object.values(additions);
								const elements_deleted = Object.values(deletions);
								//records differences
								for(let i = 0; i < elements_added.length; i++) {
									differences.push(new DifferenceArrayLength(this, property_id, true, elements_added[i]));
								}
								for(let i = 0; i < elements_deleted.length; i++) {
									differences.push(new DifferenceArrayLength(this, property_id, false, elements_deleted[i]));
								}
								//remove added and deleted elements to normalize arrays with same content, then check ordering
								const value_without_modifications = value.slice();
								value_without_modifications.removeElements(elements_modified_added);
								const other_value_without_modifications = other_value.slice();
								other_value_without_modifications.removeElements(elements_modified_deleted);
								if(!Object.equals(value_without_modifications, other_value_without_modifications)) {
									differences.push(new DifferenceArrayOrdering(this, property_id));
								}
							}
						}
						//other
						else {
							if(!Object.equals(value, other_value)) {
								differences.push(new DifferenceProperty(this, property_id, value, other_value));
							}
						}
					}
				}
			}
		}
		return differences;
	}
	/**
	 * @param {Node} other_node - The other node to compare this node with
	 * @returns {(DifferenceArrayLength|DifferenceArrayOrdering|DifferenceArrayElement|DifferenceProperty|DifferenceChild)[]} - The list of differences between the two nodes
	 */
	compare(other_node) {
		const differences = [];
		//compare it self
		differences.pushAll(this.compareSelf(other_node));
		//compare children
		for(const [child_entity_name, relation] of Object.entries(this.getEntity().children)) {
			const child_entity = Entities[child_entity_name];
			for(let n = 0; n < relation.size; n++) {
				const children = this.getChildren(child_entity, n);
				const other_children = other_node.getChildren(child_entity, n);
				//store length
				const children_length = children.length;
				const other_children_length = other_children.length;

				const child_positions = [];
				const potentially_added_child = [];
				const potentially_deleted_child = [];
				for(let i = 0; i < children_length; i++) {
					const child = children[i];
					const best_match = {index: undefined, score: undefined};
					//TODO optimize this by starting the loop at index i
					for(let j = 0; j < other_children_length; j++) {
						//one other child node can match only one child node
						if(!child_positions.includes(j)) {
							const other_child = other_children[j];
							const child_differences = child.compareSelf(other_child);
							if(child_differences === 0) {
								best_match.index = j;
								best_match.score = 0;
								break;
							}
							//calculate matching score
							let score = child_differences.length;
							let has_same_id = true;
							for(let k = 0; k < child_differences.length; k++) {
								const child_difference = child_differences[k];
								if(child_difference.constructor.name === 'DifferenceProperty' && child_difference.property === 'id') {
									has_same_id = false;
								}
							}
							//same id improve score by 5
							if(has_same_id) {
								score -= 5;
							}
							if(!best_match.score || score < best_match.score) {
								best_match.score = score;
								best_match.index = j;
							}
						}
					}
					if(best_match.score < 3) {
						child_positions.push(best_match.index);
						/*if(i !== best_match.index) {
							differences.push(new DifferenceArrayOrdering(this, child_entity_id));
						}*/
						//compare children
						differences.pushAll(child.compare(other_children[best_match.index]));
					}
					else {
						//a node has potentially been added (or the id has changed)
						potentially_added_child.push(child);
					}
				}
				for(let i = 0; i < other_children_length; i++) {
					if(!child_positions.includes(i)) {
						potentially_deleted_child.push(other_children[i]);
					}
				}
				//TODO try to find a match between potentially added nodes and deleted nodes
				for(let i = 0; i < potentially_added_child.length; i++) {
					differences.push(new DifferenceChild(this, Entities[child_entity_name], potentially_added_child[i].id, true));
				}
				for(let i = 0; i < potentially_deleted_child.length; i++) {
					differences.push(new DifferenceChild(this, Entities[child_entity_name], potentially_deleted_child[i].id, false));
				}
				//if children have same content, check ordering
				/*if(!has_content_difference && !Object.equals(children, other_children)) {
					differences.push(new DifferenceArrayOrdering(this, child_entity_id));
				}*/
			}
		}
		return differences;
	}
}
