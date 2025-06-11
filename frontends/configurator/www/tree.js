import './basic-tools/extension.js';

import {UI} from './tools/ui.js';
import {Config, ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {Router} from './router.js';
import {NodeTools} from './node_tools.js';
import {NodeContextualMenu} from './node_contextual_menu.js';
import {Entities} from './model/config/entities.js';

const SELECTION_DELAY = 200;

//tree
class Tree {

	constructor(root, exclusions) {
		//retain exclusions as config entities ids
		this.exclusions = exclusions || [];
		//build representation
		//set a tab index to be able to add key listener
		this.representation = document.createFullElement('ul', {'class': `expanded nodes ${root.getEntity().id}`, tabindex: -1});
		//keep handle on this
		const that = this;
		let selection_timeout;
		function keyboard_selection(element) {
			//select node or entity in the tree
			element.select();
			//add delay to real selection of node because some form models are heavy and slow down browsing of the tree
			//timeout is used to detect end of tree browsing
			if(selection_timeout) {
				clearTimeout(selection_timeout);
			}
			selection_timeout = setTimeout(function() {
				if(element.constructor === TreeNode) {
					Router.SelectNode(element.node);
				}
				else {
					Router.SelectEntity(element.node, element.entity);
				}
			}, SELECTION_DELAY);
		}
		//add listeners
		this.representation.addEventListener(
			'keydown',
			function(event) {
				const selected_element = that.getSelectedElement();
				if(selected_element) {
					switch(event.key) {
						case 'ArrowDown': {
							//select first child
							if(selected_element.expanded) {
								//select first child
								if(!selected_element.getChildrenDisplayed().isEmpty()) {
									keyboard_selection(selected_element.getChildrenDisplayed()[0]);
								}
							}
							else {
								const parent = selected_element.parent;
								const siblings = parent.getChildrenDisplayed();
								let index = siblings.indexOf(selected_element);
								//select next sibling if any
								if(index < siblings.length - 1) {
									keyboard_selection(siblings[++index]);
								}
								//select cousin
								else {
									let ancestor = parent;
									while(ancestor.parent) {
										const cousins = ancestor.parent.getChildrenDisplayed();
										index = cousins.indexOf(ancestor);
										if(index < cousins.length - 1) {
											keyboard_selection(cousins[++index]);
											break;
										}
										ancestor = ancestor.parent;
									}
								}
							}
							event.stop();
							break;
						}
						case 'ArrowUp': {
							const parent = selected_element.parent;
							if(parent) {
								let index = parent.getChildrenDisplayed().indexOf(selected_element);
								//select last expanded descendant in previous brother
								if(index > 0) {
									//select last non expanded descendant in previous brother
									let cousin = parent.getChildrenDisplayed()[--index];
									while(cousin.expanded && !cousin.getChildrenDisplayed().isEmpty()) {
										cousin = cousin.getChildrenDisplayed().last();
									}
									keyboard_selection(cousin);
								}
								//select parent
								else {
									keyboard_selection(parent);
								}
							}
							event.stop();
							break;
						}
						case 'ArrowLeft': {
							selected_element.collapse();
							event.stop();
							break;
						}
						case 'ArrowRight': {
							selected_element.expand();
							event.stop();
							break;
						}
						case 'Delete': {
							if(selected_element.constructor === TreeNode) {
								const node = selected_element.node;
								const node_message = node.id ? `${node.getEntity().name} ${node.id}` : `this ${node.getEntity().name}`;
								UI.Validate(`Are you sure you want to delete ${node_message}?`).then(confirmed => {
									if(confirmed) {
										node.delete();
									}
								});
								event.stop();
								break;
							}
						}
					}
				}
			}
		);
		//update tree
		bus_ui.register(this);
		//create root node
		this.root = new TreeNode(this, undefined, root);
		//root node is always expanded
		this.root.expanded = true;
		this.root.ui.list.classList.add('expanded');
		//root node cannot be fold
		this.root.ui.toggle.style.display = 'none';
		this.representation.appendChild(this.root.representation);
	}
	destroy() {
		bus_ui.unregister(this);
		function destroy_element(element) {
			bus_ui.unregister(element);
			element.childNodes.forEach(destroy_element);
			if(element.childEntities) {
				element.childEntities.forEach(destroy_element);
			}
		}
		destroy_element(this.root);
	}
	getSelectedElement() {
		const selected_link = this.representation.querySelector('a.selected');
		if(selected_link) {
			return selected_link.parentNode.parentNode.treeElement;
		}
		return undefined;
	}
	unselect() {
		const selected_element = this.getSelectedElement();
		if(selected_element) {
			selected_element.ui.link.classList.remove('selected');
		}
	}
	expand() {
		this.root.expandAll();
	}
	collapse() {
		this.root.collapseAll();
	}
	draw(place) {
		place.empty();
		place.appendChild(this.representation);
	}
	filter(filter) {
		this.root.filter(filter);
	}
	find(node) {
		return this.root.find(node);
	}
	hasNode(node) {
		return !!this.find(node);
	}
	refresh() {
		this.root.refreshAll();
	}
	scrollToSelection() {
		const selection = this.getSelectedElement();
		//selection is hidden below
		if(selection.representation.offsetTop > this.representation.clientHeight + this.representation.scrollTop) {
			this.representation.scrollTop = selection.representation.offsetTop - this.representation.clientHeight;
		}
		//selection is hidden above
		if(selection.representation.offsetTop - 40 < this.representation.scrollTop) {
			this.representation.scrollTop = selection.representation.offsetTop - 40;
		}
	}
	isEntityIncluded(entity) {
		//find if entity is not an excluded entity nor a descendant of an excluded entity
		return !this.exclusions.some(e => e === entity || entity.isDescendantOf(e));
	}
	//bus ui listeners
	onMove(event) {
		//find matching tree nodes
		const dragged_tree_node = this.find(event.node);
		const new_parent_tree_node = this.find(event.newParent);
		//node may no been appear in the tree (for example CMSWidgets are not in the tree but can be moved between CMSSections)
		//TODO improve this as the one of the node may be in the tree and not the other
		if(dragged_tree_node && new_parent_tree_node) {
			const old_parent_tree_node = dragged_tree_node.parent;
			//update tree structure
			old_parent_tree_node.removeChildNode(dragged_tree_node);
			new_parent_tree_node.addChildNode(dragged_tree_node);
			//sort child node of new parent node
			new_parent_tree_node.sort();
		}
	}
	onAddChild(event) {
		if(this.isEntityIncluded(event.child.getEntity())) {
			//find parent
			let parent_tree_node = this.find(event.parent);
			//check if child node exists in the tree
			if(this.hasNode(event.child)) {
				parent_tree_node.refresh();
			}
			//create it if it does not exist
			else {
				//find good section in parent tree node
				if(!parent_tree_node.childEntities.isEmpty()) {
					parent_tree_node = parent_tree_node.childEntities.find(c => c.entity === event.child.getEntity());
				}
				//create new tree node for new child
				const tree_node = new TreeNode(this, parent_tree_node, event.child);
				//refresh new node to sort it among its siblings
				tree_node.refresh();
			}
		}
	}
	save() {
		const state = {
			nodes: {},
			entities: {}
		};
		function save_element(element) {
			if(element.constructor === TreeNode) {
				state.nodes[element.node.getGlobalId()] = element.expanded;
			}
			else {
				//BUG wrong, an entity can be repeated, but it is not the case for now
				state.entities[element.entity.name] = element.expanded;
			}
			element.childNodes.forEach(save_element);
			if(element.childEntities) {
				element.childEntities.forEach(save_element);
			}
		}
		save_element(this.root);
		return state;
	}
	restore(state) {
		function restore_element(element) {
			if(element.constructor === TreeNode) {
				state.nodes[element.node.getGlobalId()] ? element.expand() : element.collapse();
			}
			else {
				state.entities[element.entity.name] ? element.expand() : element.collapse();
			}
			element.childNodes.forEach(restore_element);
			if(element.childEntities) {
				element.childEntities.forEach(restore_element);
			}
		}
		restore_element(this.root);
	}
}

//tree element
class TreeElement {
	constructor(tree) {
		//relations
		this.tree = tree;
		this.childNodes = [];
		this.childEntities = [];
		this.parent = undefined;
		//ui
		this.representation = undefined;
		this.ui = {};
		this.expanded = false;
		//add in bus
		bus_ui.register(this);
	}
	//add and remove child
	addChildNode(child_node) {
		//manage tree structure
		child_node.parent = this;
		//if tree element is a tree node and has children tree entities, find the appropriate tree entity in which the child node must be added
		if(this.hasEntities()) {
			this.childEntities.find(c => c.entity === child_node.node.getEntity()).addChildNode(child_node);
		}
		else {
			this.childNodes.push(child_node);
			//manage ui
			this.ui.list.appendChild(child_node.representation);
			this.ui.toggle.style.visibility = 'visible';
		}
	}
	removeChildNode(child_node) {
		//manage tree structure
		child_node.parent = undefined;
		this.childNodes.removeElement(child_node);
		//manage ui
		this.ui.list.removeChild(child_node.representation);
		if(this.childNodes.isEmpty()) {
			this.collapse();
			this.ui.toggle.style.visibility = 'hidden';
		}
	}
	getChildNode(node) {
		return this.childNodes.find(c => c.node === node);
	}
	getChildren() {
		//must be implemented
		return [];
	}
	getChildrenDisplayed() {
		return this.getChildren().filter(n => n.isDisplayed());
	}
	getChildrenComparator() {
		//must be implemented
		throw new Error('Must be implemented');
	}
	hasEntities() {
		//must be implemented
		return false;
	}
	//ui
	isDisplayed() {
		return !this.representation.classList.contains('hidden');
	}
	represent() {
		//must be implemented
		throw new Error('Must be implemented');
	}
	select() {
		this.tree.unselect();
		this.ui.link.focus();
		this.ui.link.classList.add('selected');
		this.tree.scrollToSelection();
	}
	highlight() {
		//expand all parent entity nodes
		let parent = this;
		while(parent.parent) {
			parent = parent.parent;
			parent.expand();
		}
		this.select();
	}
	//expand and collapse
	expand() {
		if(this.tree.root !== this) {
			if(this.ui.list && !this.getChildren().isEmpty()) {
				this.expanded = true;
				this.ui.list.classList.add('expanded');
				this.ui.toggle.classList.add('expanded');
				this.ui.toggle.setAttributes({
					alt: 'Fold',
					title: 'Fold'
				});
			}
		}
	}
	collapse() {
		if(this.tree.root !== this) {
			if(this.ui.list && !this.getChildren().isEmpty()) {
				this.expanded = false;
				this.ui.list.classList.remove('expanded');
				this.ui.toggle.classList.remove('expanded');
				this.ui.toggle.setAttributes({
					alt: 'Unfold',
					title: 'Unfold'
				});
			}
		}
	}
	//sort
	sort() {
		if(!this.childNodes.isEmpty()) {
			const comparator_model = this.getChildrenComparator();
			if(comparator_model !== undefined) {
				const children_list = this.ui.list;
				//extract children
				const siblings = children_list.childNodes.slice();
				//sort children
				const comparator = comparator_model.call(undefined, Languages.GetLanguage());
				siblings.sort((n1, n2) => comparator(n1.treeElement.node, n2.treeElement.node));
				//re-import children
				siblings.forEach(s => children_list.appendChild(s));
				//update tree structure
				this.childNodes.sort((n1, n2) => comparator(n1.node, n2.node));
			}
		}
	}
}

//tree node
class TreeNode extends TreeElement {
	constructor(tree, parent, node) {
		super(tree);
		//data
		this.node = node;
		//build representation
		this.representation = this.represent();
		if(parent) {
			parent.addChildNode(this);
		}
	}
	hasEntities() {
		return !this.childEntities.isEmpty();
	}
	//interface to get children
	getChildren() {
		return this.hasEntities() ? this.childEntities : this.childNodes;
	}
	addChildEntity(child_entity) {
		//manage tree structure
		child_entity.parent = this;
		this.childEntities.push(child_entity);
		//manage ui
		this.ui.list.appendChild(child_entity.representation);
		this.ui.toggle.style.visibility = 'visible';
	}
	//retrieve children
	getChildEntity(entity_id) {
		return this.childEntities.find(c => c.entity.id === entity_id);
	}
	//expand and collapse all
	expandAll() {
		this.expand();
		this.childNodes.forEach(c => c.expandAll());
		this.childEntities.forEach(c => c.expandAll());
	}
	collapseAll() {
		this.collapse();
		this.childNodes.forEach(c => c.collapseAll());
		this.childEntities.forEach(c => c.collapseAll());
	}
	//filter
	filter(filter) {
		let found;
		//in any case, filter child entities and child nodes to un-hide nodes what could have been hidden by a previous filter
		if(this.childEntities.map(c => c.filter(filter)).includes(true)) {
			found = true;
		}
		if(this.childNodes.map(c => c.filter(filter)).includes(true)) {
			found = true;
		}
		//empty filter
		if(!filter) {
			found = true;
		}
		//if no matching node has been found in descendants, try with current node
		else if(!found) {
			found = filter(this.node);
		}
		//manage ui
		if(found) {
			this.expand();
			this.representation.classList.remove('hidden');
		}
		else {
			this.collapse();
			this.representation.classList.add('hidden');
		}
		return found;
	}
	//find
	find(node) {
		if(this.node === node) {
			return this;
		}
		//find children
		let child_nodes = this.childNodes.slice();
		child_nodes = child_nodes.concat(...this.childEntities.map(c => c.childNodes));
		for(let i = 0; i < child_nodes.length; i++) {
			const result = child_nodes[i].find(node);
			if(result) {
				return result;
			}
		}
		return undefined;
	}
	//child comparator
	getChildrenComparator() {
		return this.childNodes[0].node.constructor.getComparator;
	}
	//bus ui listeners
	onDelete(event) {
		if(event.node === this.node) {
			this.parent.removeChildNode(this);
		}
	}
	//refresh
	refresh() {
		const entities = Object.keys(this.node.getEntity().children).map(e => Entities[e]);
		entities.removeElements(this.tree.exclusions);
		if(!entities.isEmpty()) {
			//refresh child nodes
			if(entities.length === 1) {
				//make model and ui match by adding new child tree node and removing old ones
				const child_entity = entities[0];
				const nodes = this.node.getChildren(child_entity).slice();
				//sort nodes
				if(Config.Entities[child_entity.name].getComparator) {
					const comparator = Config.Entities[child_entity.name].getComparator(Languages.GetLanguage());
					nodes.sort(comparator);
				}
				//add new node if required
				nodes.forEach(function(node) {
					const tree_node = this.getChildNode(node);
					if(tree_node) {
						this.ui.list.appendChild(tree_node.representation);
					}
					//tree node does not exist yet
					else {
						new TreeNode(this.tree, this, node);
					}
				}, this);
				//delete old nodes
				this.childNodes.filter(c => !nodes.includes(c.node)).forEach(c => this.removeChildNode(c));
			}
			//refresh child entities
			else {
				this.childEntities.forEach(c => c.refresh());
			}
		}
		//sort tree node among its siblings (useless for root node)
		if(this.parent) {
			this.parent.sort();
		}
	}
	refreshAll() {
		//sort child nodes
		this.sort();
		//do the same for child nodes
		this.childNodes.forEach(c => c.refreshAll());
		this.childEntities.flatMap(c => c.childNodes).forEach(c => c.refreshAll());
	}
	represent() {
		const that = this;

		//container
		const node_container = document.createElement('li');
		node_container.treeElement = this;

		//node container
		const node_label_container = document.createElement('span');

		//toggle
		this.ui.toggle = document.createFullElement('img', {src: 'images/bullet_arrow_right.png', alt: 'Unfold', title: 'Unfold'});
		this.ui.toggle.addEventListener('click', toggle_click_listener);
		node_label_container.appendChild(this.ui.toggle);

		//node
		this.ui.link = NodeTools.Draw(this.node, undefined, true);
		this.ui.link.setAttribute('tabindex', -1);
		this.ui.link.addEventListener('contextmenu', node_context_menu_listener);
		node_label_container.appendChild(this.ui.link);
		node_container.appendChild(node_label_container);

		//retrieve child entities
		const entities = Object.keys(this.node.getEntity().children).map(e => Entities[e]);
		entities.removeElements(this.tree.exclusions);

		if(entities.isEmpty()) {
			this.ui.toggle.style.display = 'none';
		}
		else {
			this.ui.toggle.style.display = 'inline';

			if(entities.length === 1) {
				//add child node button
				const child_entity = entities[0];
				const node_li_add = document.createFullElement('img', {alt: 'Add', title: `Add ${child_entity.label.toLowerCase()}`, src: 'images/add.png'});
				node_li_add.addEventListener('click', function(event) {
					//stop propagation so this node won't be selected and prevent default so anchor link won't be used
					event.stop();
					//show toggle
					that.ui.toggle.style.visibility = 'visible';
					//retrieve existing nodes
					const siblings = that.node.getChildren(child_entity);
					//create node only if a blank node does not already exist
					if(siblings.every(s => s.getLocalizedLabel(Languages.GetLanguage()))) {
						const node = new Config.Entities[child_entity.name]();
						//add node in parent
						that.node.addChild(node);
						//show edition form
						Router.SelectNode(node);
						//notify
						UI.Notify(`New ${child_entity.label.toLowerCase()} created successfully`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
					}
					else {
						//notify
						UI.Notify(`There is already an empty ${child_entity.label.toLowerCase()}`, {tag: 'warning', icon: 'images/notifications_icons/warning.svg'});
					}
				});
				node_label_container.appendChild(node_li_add);

				//draw child nodes
				//create nodes container
				this.ui.list = document.createFullElement('ul', {'class': `nodes ${child_entity.id}`});
				node_container.appendChild(this.ui.list);

				//retrieve child nodes for entity
				const nodes = this.node.getChildren(child_entity).slice();
				if(nodes.isEmpty()) {
					this.ui.toggle.style.visibility = 'hidden';
				}
				else {
					this.ui.toggle.style.visibility = 'visible';
					if(Config.Entities[child_entity.name].getComparator) {
						const comparator = Config.Entities[child_entity.name].getComparator(Languages.GetLanguage());
						nodes.sort(comparator);
					}
					nodes.forEach(n => new TreeNode(this.tree, this, n));
				}
			}
			else {
				//draw entities
				this.ui.toggle.style.visibility = 'visible';
				//create entities container
				this.ui.list = document.createFullElement('ul', {'class': 'entities'});
				node_container.appendChild(this.ui.list);

				//draw each entity
				entities.forEach(e => new TreeEntity(this.tree, this, this.node, e));
			}
		}
		return node_container;
	}
}

//tree entity
class TreeEntity extends TreeElement {
	constructor(tree, parent, node, entity) {
		super(tree);
		//data
		this.node = node;
		this.entity = entity;
		//build representation
		this.representation = this.represent();
		if(parent) {
			parent.addChildEntity(this);
		}
	}
	hasEntities() {
		return false;
	}
	getChildren() {
		return this.childNodes;
	}
	expandAll() {
		this.expand();
		this.childNodes.forEach(c => c.expandAll());
	}
	collapseAll() {
		this.collapse();
		this.childNodes.forEach(c => c.collapseAll());
	}
	filter(filter) {
		let found = false;
		//search in all child nodes
		if(this.childNodes.map(c => c.filter(filter)).includes(true)) {
			found = true;
		}
		//empty filter
		if(!filter) {
			found = true;
		}
		//manage ui
		if(found) {
			this.expand();
			this.representation.classList.remove('hidden');
		}
		else {
			this.collapse();
			this.representation.classList.add('hidden');
		}
		return found;
	}
	getChildrenComparator() {
		return Config.Entities[this.entity.name].getComparator;
	}
	//bus ui listeners
	onChange(event) {
		if(event.property === 'id' && event.node === this.node || this.node.isDescendantOf(event.node)) {
			this.ui.link.setAttribute('href', `#node=${this.node.getGlobalId()}&entity=${this.entity.name}`);
		}
	}
	onMove(event) {
		if(event.node === this.node || this.node.isDescendantOf(event.node)) {
			this.ui.link.setAttribute('href', `#node=${this.node.getGlobalId()}&entity=${this.entity.name}`);
		}
	}
	refresh() {
		const nodes = this.parent.node.getChildren(this.entity).slice();
		//add new nodes if required
		nodes.forEach(function(node) {
			let tree_node = this.getChildNode(node);
			if(!tree_node) {
				//tree node does not exist yet
				tree_node = new TreeNode(this.tree, this, node);
			}
			this.ui.list.appendChild(tree_node.representation);
		}, this);
		//delete old nodes
		this.childNodes.filter(c => !nodes.includes(c.node)).forEach(c => this.removeChildNode(c));
		this.sort();
	}
	represent() {
		const that = this;

		//container
		const entity_container = document.createFullElement('li');
		entity_container.treeElement = this;

		//entity container
		const entity_label_container = document.createElement('span');

		//toggle
		this.ui.toggle = document.createFullElement('img', {src: 'images/bullet_arrow_right.png', alt: 'Unfold', title: 'Unfold'});
		this.ui.toggle.addEventListener('click', toggle_click_listener);
		entity_label_container.appendChild(this.ui.toggle);

		//entity
		this.ui.link = document.createFullElement('a', {href: `#node=${this.node.getGlobalId()}&entity=${this.entity.name}`, tabindex: -1});
		const icon = Function.isFunction(this.entity.icon) ? this.entity.icon.call() : this.entity.icon;
		this.ui.link.appendChild(document.createFullElement('img', {src: `images/entities_icons/${icon}`, alt: this.entity.label, style: 'margin-right: 2px;'}));
		this.ui.link.appendChild(document.createTextNode(this.entity.plural_label));
		this.ui.link.addEventListener('contextmenu', entity_context_menu_listener);
		this.ui.link.addEventListener('dragenter', entity_dragover);
		this.ui.link.addEventListener('dragover', entity_dragover);
		this.ui.link.addEventListener('dragleave', entity_dragleave);
		this.ui.link.addEventListener('drop', entity_drop);
		entity_label_container.appendChild(this.ui.link);

		//add child node button
		const entity_li_add = document.createFullElement('img', {alt: 'Add', title: `Add ${this.entity.label.toLowerCase()}`, src: 'images/add.png'});
		entity_li_add.addEventListener('click', event => {
			//stop propagation so this node won't be selected and prevent default so anchor link won't be used
			event.stop();
			//show toggle
			that.ui.toggle.style.visibility = 'visible';
			//retrieve existing nodes
			const siblings = this.parent.node.getChildren(this.entity);
			//create node only if a blank node does not already exist
			if(siblings.every(s => s.getLocalizedLabel(Languages.GetLanguage()))) {
				const node = new Config.Entities[this.entity.name]();
				//add node in parent
				this.parent.node.addChild(node);
				//show edition form
				Router.SelectNode(node);
				//notify
				UI.Notify(`New ${this.entity.label.toLowerCase()} created successfully`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
			}
			else {
				//notify
				UI.Notify(`There is already an empty ${this.entity.label.toLowerCase()}`, {tag: 'warning', icon: 'images/notifications_icons/warning.svg'});
			}
		});
		entity_label_container.appendChild(entity_li_add);
		entity_container.appendChild(entity_label_container);

		//draw nodes
		const children_nodes_list = document.createFullElement('ul', {'class': `nodes ${this.entity.id}`});
		entity_container.appendChild(children_nodes_list);
		this.ui.list = children_nodes_list;
		//retrieve child nodes for entity
		const nodes = this.node.getChildren(this.entity).slice();
		if(nodes.isEmpty()) {
			this.ui.toggle.style.visibility = 'hidden';
		}
		else {
			this.ui.toggle.style.visibility = 'visible';
			if(this.getChildrenComparator()) {
				const comparator = this.getChildrenComparator().call(undefined, Languages.GetLanguage());
				nodes.sort(comparator);
			}

			nodes.forEach(n => new TreeNode(this.tree, this, n));
		}
		return entity_container;
	}
}

function toggle_click_listener() {
	const tree_element = this.parentNode.parentNode.treeElement;
	//toggle node
	tree_element.expanded ? tree_element.collapse() : tree_element.expand();
}

function entity_dragover(event) {
	if(event.dataTransfer.types.includes('Files')) {
		event.preventDefault();
		//event.dataTransfer.dropEffect = 'move';
		this.classList.add('dragover');
	}
}

function entity_dragleave() {
	this.classList.remove('dragover');
}

function entity_drop(event) {
	event.preventDefault();
	this.classList.remove('dragover');
	//retrieve drop entity and drop node
	const tree_entity = this.parentNode.parentNode.treeElement;
	const drop_entity = tree_entity.entity;
	const drop_node = tree_entity.parent.node;
	//only one file can be managed at a time
	if(event.dataTransfer.files.length > 1) {
		UI.Notify('Drop only one file', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
	}
	else {
		const file = event.dataTransfer.files[0];
		if(file.type && file.type !== 'application/json') {
			UI.Notify('File not supported', {
				tag: 'error',
				icon: 'images/notifications_icons/warning.svg',
				body: `Invalid mime type ${file.type}. Only JSON files are supported.`
			});
		}
		else {
			const reader = new FileReader();
			reader.addEventListener(
				'loadstart',
				function() {
					UI.StartLoading();
				}
			);
			reader.addEventListener(
				'error',
				function() {
					UI.Notify(`Error while loading ${file.name}`, {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
				}
			);
			reader.addEventListener(
				'load',
				function(reader_event) {
					try {
						//revive node
						const data = JSON.parse(reader_event.target.result);
						const node = ConfigHelpers.Reviver(data);
						//check if node has the right entity
						if(drop_entity === node.constructor) {
							//find id
							let node_id = node.id;
							while(drop_node.getHasChild(node.getEntity(), undefined, node_id)) {
								node_id = `COPY_${node_id}`;
							}
							//set id
							bus.disable();
							node.id = node_id;
							bus.enable();
							//add node as a child of drop node
							drop_node.addChild(node);
						}
						else {
							UI.Notify('Node cannot be dropped here', {
								tag: 'error',
								icon: 'images/notifications_icons/warning.svg',
								body: `Unable to add a ${node.getEntity().label} in ${drop_entity.getEntity().label}.`
							});
						}
					}
					catch(exception) {
						console.log(exception);
						UI.Notify('Invalid JSON file', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
					}
				}
			);
			reader.addEventListener(
				'loadend',
				function() {
					UI.StopLoading();
				}
			);
			reader.readAsText(file);
		}
	}
}

function entity_context_menu_listener(event) {
	event.preventDefault();
	this.classList.add('chosen');
	//retrieve node
	const node = this.parentNode.parentNode.treeElement.parent.node;
	NodeContextualMenu.OpenEntityMenu(event, node, () => this.classList.remove('chosen'));
}

function node_context_menu_listener(event) {
	event.preventDefault();
	this.classList.add('chosen');
	//retrieve node
	const node = this.parentNode.parentNode.treeElement.tree.root.node.getNode(this.dataset.nodeGlobalId);
	NodeContextualMenu.OpenNodeMenu(event, node, () => this.classList.remove('chosen'));
}

export {Tree, TreeElement, TreeEntity, TreeNode};
