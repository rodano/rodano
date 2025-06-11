import {StudyHandler} from './study_handler.js';
import {Entities} from './model/config/entities.js';
import {SelectionNode} from './model/config/entities/selection_node.js';
import {NodeTools} from './node_tools.js';
import {bus_ui} from './bus_ui.js';
import {Router} from './router.js';

const TOGGLE = {
	selected: {
		image: 'images/tick.png',
		label: 'Unselect'
	},
	unselected: {
		image: 'images/untick.png',
		label: 'Select'
	}
};

function get_node_from_chain(root, chain) {
	if(chain.isEmpty()) {
		return root;
	}
	const sub_chain = chain.slice();
	const link = sub_chain.shift();
	const child = get_children(root).find(c => c.id === link.id);
	return get_node_from_chain(child, sub_chain);
}

function get_li_from_chain(ul, chain) {
	const link = chain[0];
	const li = ul.children.find(c => c.dataset.entity === link.entity && c.dataset.id === link.id);
	const sub_chain = chain.slice();
	sub_chain.shift();
	if(sub_chain.isEmpty()) {
		return li;
	}
	return get_li_from_chain(li.querySelector('ul'), sub_chain);
}

function get_chain_from_li(li) {
	const link = {entity: li.dataset.entity, id: li.dataset.id};
	if(link.entity === SelectionNode.SELECTABLE_ENTITIES[0].name) {
		return [link];
	}
	return [...get_chain_from_li(li.parentNode.parentNode), link];
}

function is_selected(chain) {
	let container = StudyHandler.GetStudy();
	for(let i = 0; i < chain.length; i++) {
		const link = chain[i];
		container = container.selections.find(s => s.nodeEntity === Entities[link.entity].configuration_name && s.nodeId === link.id);
		//if the container has not been found, it's because it is not selected
		if(!container) {
			return false;
		}
	}
	return true;
}

function add_chain(chain) {
	let container = StudyHandler.GetStudy();
	for(let i = 0; i < chain.length; i++) {
		const link = chain[i];
		const entity = Entities[link.entity];
		let sub_container = container.selections.find(s => s.nodeEntity === entity.configuration_name && s.nodeId === link.id);
		//if the container has not been found, it's because it is not selected yet and it must be created
		//this selects all parents until root
		if(!sub_container) {
			sub_container = new SelectionNode({nodeEntity: entity.configuration_name, nodeId: link.id});
			container.addChild(sub_container);
		}
		container = sub_container;
	}
}

function remove_chain(chain) {
	let container = StudyHandler.GetStudy();
	for(let i = 0; i < chain.length; i++) {
		const link = chain[i];
		container = container.selections.find(s => s.nodeEntity === Entities[link.entity].configuration_name && s.nodeId === link.id);
	}
	container.delete();
}

function toggle_chain(chain, state) {
	if(state === TOGGLE.selected) {
		add_chain(chain);
		//select all children
		const entity = Entities[chain.last().entity];
		const entity_index = SelectionNode.SELECTABLE_ENTITIES.indexOf(entity);
		if(entity_index < SelectionNode.SELECTABLE_ENTITIES.length) {
			const node = get_node_from_chain(StudyHandler.GetStudy(), chain);
			const children = get_children(node);
			const child_entity = SelectionNode.SELECTABLE_ENTITIES[entity_index + 1];
			children.map(c => [...chain, {entity: child_entity.name, id: c.id}]).forEach(c => toggle_chain(c, state));
		}
	}
	else {
		remove_chain(chain);
	}
}

function toggle_node_listener() {
	const state = this.src.includes(TOGGLE.unselected.image) ? TOGGLE.selected : TOGGLE.unselected;
	const container = this.parentNode.parentNode;
	toggle_chain(get_chain_from_li(container), state);
}

function get_children(node) {
	const entity = node.getEntity();
	switch(entity) {
		case Entities.Study:
			return node.scopeModels;
		case Entities.ScopeModel:
			return node.eventModels;
		case Entities.EventModel:
			return node.getFormModels();
		case Entities.FormModel:
			return node.layouts;
	}
	return [];
}

function draw_child_nodes(chain, node) {
	//retrieve children
	const children = get_children(node);
	const ul = document.createElement('ul');
	children.map(draw_node.bind(undefined, chain)).forEach(Node.prototype.appendChild, ul);
	return ul;
}

function draw_node(chain, node) {
	const entity = node.getEntity();
	const link = {entity: entity.name, id: node.id};
	const sub_chain = [...chain, link];

	const toggle = document.createFullElement('img');
	const state = is_selected(sub_chain) ? TOGGLE.selected : TOGGLE.unselected;
	toggle.setAttributes({src: state.image, alt: state.label, title: state.label});
	toggle.addEventListener('click', toggle_node_listener);

	const span = document.createElement('span');
	span.appendChild(NodeTools.Draw(node, undefined, true));
	span.appendChild(toggle);

	const li = document.createFullElement('li', {'data-entity': entity.name, 'data-id': node.id});
	li.appendChild(span);
	li.appendChild(draw_child_nodes(sub_chain, node));

	return li;
}

function delete_recursive(selection) {
	const chain = selection.getChain();
	const li = get_li_from_chain(document.querySelector('#selector > ul'), chain);
	const toggle = li.querySelector('span > img');
	toggle.setAttributes({src: TOGGLE.unselected.image, alt: TOGGLE.unselected.label, title: TOGGLE.unselected.label});
	selection.selections.forEach(delete_recursive);
}

export const DocumentationSelection = {
	Init: function() {
		//documentation selection dialog is managed by the application URL
		/**@type {HTMLDialogElement}*/ (document.getElementById('selection')).addEventListener('close', () => Router.CloseTool());

		document.getElementById('selection_reset').addEventListener('click', () => StudyHandler.GetStudy().selections.forEach(s => s.delete()));
		//register following listeners in the bus
		//this code will receive the bus events even if the documentation panel is not displayed
		bus_ui.register({
			onAddChild: event => {
				//TODO this is a hack to handle events only when the documentation is open
				if(document.getElementById('selection').offsetWidth > 0) {
					if(event.child.getEntity() === Entities.SelectionNode) {
						const chain = event.child.getChain();
						const li = get_li_from_chain(document.querySelector('#selector > ul'), chain);
						const toggle = li.querySelector('span > img');
						toggle.setAttributes({src: TOGGLE.selected.image, alt: TOGGLE.selected.label, title: TOGGLE.selected.label});
					}
				}
			},
			onDelete: event => {
				//TODO this is a hack to handle events only when the documentation is open
				if(document.getElementById('selection').offsetWidth > 0) {
					if(event.node.getEntity() === Entities.SelectionNode) {
						delete_recursive(event.node);
					}
				}
			}
		});
	},
	Open: function() {
		const study = StudyHandler.GetStudy();
		const selector = document.getElementById('selector');
		selector.empty();
		selector.appendChild(draw_child_nodes([], study));
		/**@type {HTMLDialogElement}*/ (document.getElementById('selection')).showModal();
	}
};
