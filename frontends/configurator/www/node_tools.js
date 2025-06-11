import './basic-tools/extension.js';

import {Bus} from './basic-tools/bus.js';
import {UI} from './tools/ui.js';
import {BusEventMoveNode} from './model/config/entities_config_bus.js';
import {ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Languages} from './languages.js';
import {bus_ui} from './bus_ui.js';
import {StudyHandler} from './study_handler.js';
import {Favorites} from './favorites.js';
import {Templates} from './templates.js';
import {Entities} from './model/config/entities.js';
import {MediaTypes} from './media_types.js';
import {Templatables} from './model/config/entities_categories.js';

//drag
function dragstart(event) {
	const node = StudyHandler.GetStudy().getNode(this.dataset.nodeGlobalId);
	NodeTools.FillDataTransfer(event.dataTransfer, node);
	event.dataTransfer.setDragImage(this, -5, -5);
	event.dataTransfer.effectAllowed = 'linkMove';
	//update style
	this.classList.add('dragover');
}

function dragend() {
	//update data attribute
	this.dataset.nodeId = this.node.id;
	this.dataset.nodeGlobalId = this.node.getGlobalId();
	//update style
	this.classList.remove('dragover');
}

//drop
function check_valid_node(dragged_node, drop_node) {
	//check dragged node entity is a child of drop node entity
	if(drop_node.getEntity().children?.hasOwnProperty(dragged_node.getEntity().name)) {
		//check there is not already a node with same id in drop node
		if(!dragged_node.id || !drop_node.getHasChild(dragged_node.getEntity(), undefined, dragged_node.id)) {
			return true;
		}
	}
	return false;
}

function dragover(event) {
	let allow_drop = false;
	//check drop availability in some cases
	if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
		//BUG some browsers do not allow to check what is dragged during drag https://bugs.webkit.org/show_bug.cgi?id=58206 or http://code.google.com/p/chromium/issues/detail?id=50009
		//check if drop is possible only on browsers that allow sniffing data
		if(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID)) {
			try {
				const dragged_node = StudyHandler.GetStudy().getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
				//check that drop is possible
				if(dragged_node && check_valid_node(dragged_node, this.node)) {
					allow_drop = true;
				}
			}
			catch {
				//node comes from an other config, no way to check if drop is possible
			}
		}
		//SHORTCUT allow drop for browsers that don't allow sniffing data
		else {
			console.warn('Unable to sniff data, allowing item drop');
			allow_drop = true;
		}
		//ENDSHORTCUT
	}
	//allow some kind of dragging
	else if(event.dataTransfer.types.includes('Files') || event.dataTransfer.types.includes('application/json')) {
		allow_drop = true;
	}
	if(allow_drop) {
		event.preventDefault();
		//event.dataTransfer.dropEffect = 'move';
		this.classList.add('dragover');
	}
}

function dragleave() {
	this.classList.remove('dragover');
}

function drop_serialized_node(drop_node, serialized_data) {
	try {
		const data = JSON.parse(serialized_data);
		const node = ConfigHelpers.Revive(data);
		//check if node can be added in drop node
		if(drop_node.getEntity().children.hasOwnProperty(node.getEntity().name)) {
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
				body: `Unable to add a ${node.getEntity().label} in ${drop_node.getEntity().label}.`
			});
		}
	}
	catch(exception) {
		console.log(exception);
		UI.Notify('Invalid JSON file', {tag: 'error', icon: 'images/notifications_icons/warning.svg'});
	}
}

function drop(event) {
	event.preventDefault();
	dragleave.call(this);
	//retrieve drop node
	const drop_node = this.node;
	//dropping a node global id
	if(event.dataTransfer.types.includes(MediaTypes.NODE_GLOBAL_ID)) {
		try {
			//retrieve dragged node
			const dragged_node = StudyHandler.GetStudy().getNode(event.dataTransfer.getData(MediaTypes.NODE_GLOBAL_ID));
			//retrieve dragged parent node and dragged parent tree node
			const dragged_node_parent = dragged_node.getParent();
			//check again that drop is possible because of webkit based browser which would have allowed a drop whereas it should not have been
			if(dragged_node && check_valid_node(dragged_node, drop_node)) {
				//update model
				bus.dispatch(new BusEventMoveNode(dragged_node.getEntity().name, dragged_node, dragged_node_parent, drop_node));
			}
			//drop managed successfully
			return;
		}
		catch(exception) {
			//node comes from an other config
			console.error(exception);
		}
	}
	//dropping a serialized node in plain json or inside a file
	if(event.dataTransfer.types.includes('Files') || event.dataTransfer.types.includes('application/json')) {
		//retrieve serialized node
		if(event.dataTransfer.types.includes('application/json')) {
			drop_serialized_node(drop_node, event.dataTransfer.getData('application/json'));
		}
		else {
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
							drop_serialized_node(drop_node, reader_event.target.result);
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
	}
}

function generate_node_title_label(node) {
	return [
		document.createTextNode(`${node.getEntity().label} `),
		NodeTools.Draw(node)
	];
}

export const NodeTools = {
	Init: function() {
		//observe document to remove node links from bus when they are removed from the DOM
		const observer = new MutationObserver(function(mutations) {
			//mutations arrive in batch and contain removed and added node
			//only keep nodes that are removed and not re-added
			const removed_nodes = [];
			mutations.forEach(function(mutation) {
				removed_nodes.pushAll(Array.prototype.slice.call(mutation.removedNodes));
				removed_nodes.removeElements(Array.prototype.slice.call(mutation.addedNodes));
			});
			//remove removed nodes from bus
			removed_nodes.forEach(function(removed_node) {
				//manage only special removed node that may be or contain a node link
				if(removed_node.constructor === HTMLAnchorElement || removed_node.childNodes.length > 0) {
					bus_ui.listeners
						.filter(l => l.constructor === HTMLAnchorElement && removed_node.contains(l))
						.forEach(Bus.prototype.unregister, bus_ui);
				}
			});
		});
		observer.observe(document.body, {childList: true, subtree: true});
	},
	FillDataTransfer(transfer, node) {
		transfer.setData('text/plain', node.id);
		transfer.setData(MediaTypes.NODE_ID, node.id);
		transfer.setData(MediaTypes.NODE_GLOBAL_ID, node.getGlobalId());
		const node_data = JSON.stringify(node, undefined, '\t');
		transfer.setData('application/json', node_data);
		//set special data transfer attribute so node will be downloaded as a file when dropped in OS, see http://www.thecssninja.com/javascript/gmail-dragout
		//IMPROVEMENT this does not work on Firefox, see https://bugzilla.mozilla.org/show_bug.cgi?id=570164
		transfer.setData('DownloadURL', `application/json:${node.id}.json:data:application/json;base64,${window.btoa(unescape(encodeURIComponent(node_data)))}`);
		//TODO create a blob file and map it to an url
		//IMPROVEMENT generate file in javascript : http://eligrey.com/blog/post/saving-generated-files-on-the-client-side
	},
	UpdateLink: function(link, node, label_type) {
		const linked_node = node || link.node;
		//retrieve and update label
		let label;
		//all node don't have method to retrieve label for a type
		if(label_type && linked_node.getLocalizedLabelForType) {
			label = linked_node.getLocalizedLabelForType(label_type, Languages.GetLanguage());
		}
		else {
			label = linked_node.getLocalizedLabel(Languages.GetLanguage());
		}
		//find and update label in current representation
		link.childNodes.find(n => n.nodeType === 3).nodeValue = label || '';
		//update attributes
		const global_id = linked_node.getGlobalId();
		link.setAttributes({
			href: `#node=${global_id}`,
			draggable: 'true',
			rel: 'edit-form',
			'data-node-global-id': global_id
		});
		if(linked_node.id) {
			link.setAttributes({
				title: linked_node.id,
				'data-node-id': linked_node.id,
			});
		}
	},
	UpdateRepresentation: function(link, node) {
		//custom representation coming from entity UI
		node.getEntity().representation?.call(node, link);
	},
	MakeDraggable: function(element, node) {
		const global_id = node.getGlobalId();
		element.setAttribute('draggable', 'true');
		element.dataset.nodeGlobalId = global_id;
		if(node.id) {
			element.dataset.nodeId = node.id;
		}
		element.addEventListener('dragstart', dragstart);
		element.addEventListener('dragend', dragend);
	},
	MakeDroppable: function(element, node) {
		const global_id = node.getGlobalId();
		element.setAttribute('draggable', 'true');
		element.dataset.nodeGlobalId = global_id;
		if(node.id) {
			element.dataset.nodeId = node.id;
		}
		element.addEventListener('dragenter', dragover);
		element.addEventListener('dragover', dragover);
		element.addEventListener('dragleave', dragleave);
		element.addEventListener('drop', drop);
	},
	Draw: function(node, label_type, with_icon) {
		const link = document.createElement('a');
		link.node = node;
		//add icon if asked
		if(with_icon) {
			const entity = node.getEntity();
			const icon = Function.isFunction(entity.icon) ? entity.icon.call(undefined, node) : entity.icon;
			link.appendChild(document.createFullElement('img', {src: `images/entities_icons/${icon}`, alt: entity.label, draggable: 'false', style: 'vertical-align: text-top; margin-right: 2px;'}));
		}
		//add placeholder for label
		link.appendChild(document.createTextNode(''));
		//enhance link
		NodeTools.UpdateLink(link, node, label_type);
		//add listeners
		//drag
		link.addEventListener('dragstart', dragstart);
		link.addEventListener('dragend', dragend);
		//drop
		link.addEventListener('dragenter', dragover);
		link.addEventListener('dragover', dragover);
		link.addEventListener('dragleave', dragleave);
		link.addEventListener('drop', drop);
		//register link on bus
		link.onChange = function(event) {
			//link must be updated when any property of the node is updated or if the id of an ancestor is updated
			if(event.node === node || event.property === 'id' && node.isDescendantOf(event.node)) {
				NodeTools.UpdateLink(this, node, label_type);
			}
			//update entity representation if properties of the node or properties of linked nodes (through the entity tree) are updated
			//that's because update of other nodes may update the usage of the current node
			if(event.node === node || event.node.getEntity().isRelatedTo(node.getEntity())) {
				NodeTools.UpdateRepresentation(this, node);
			}
		};
		link.onMove = function(event) {
			if(event.node === node || node.isDescendantOf(event.node)) {
				let global_id = this.dataset.nodeGlobalId;
				global_id = global_id.replace(`${event.oldParent.getEntity().name}:${event.oldParent.id}`, `${event.oldParent.getEntity().name}:${event.newParent.id}`);
				this.dataset.nodeGlobalId = global_id;
				this.setAttribute('href', `#node=${global_id}`);
			}
		};
		//custom representation
		NodeTools.UpdateRepresentation(link, node);
		bus_ui.register(link);
		return link;
	},

	DrawUsage: function(node, place) {
		if(!Object.isEmpty(node.getEntity().relations)) {
			//reset place
			place.empty();
			place.style.color = '';
			//retrieve usage
			const usage = node.getUsage();
			if(Object.isEmpty(usage)) {
				place.style.color = 'red';
				place.appendChild(document.createTextNode('Unused'));
			}
			else {
				place.appendChild(document.createTextNode('Used by'));
				const entities_list = document.createFullElement('ul');
				for(const [entity_name, entity_usage] of Object.entries(usage)) {
					const entity = Entities[entity_name];
					const icon = Function.isFunction(entity.icon) ? entity.icon.call(undefined) : entity.icon;
					const entity_li = document.createFullElement('li');
					entity_li.appendChild(document.createFullElement('img', {src: `images/entities_icons/${icon}`, alt: entity.label}));
					entity_li.appendChild(document.createTextNode(`${entity.label}: `));
					entities_list.appendChild(entity_li);
					entity_usage.forEach((node, index) => {
						if(index > 0) {
							entity_li.appendChild(document.createTextNode(', '));
						}
						entity_li.appendChild(NodeTools.Draw(node));
					});
				}
				place.appendChild(entities_list);
			}
		}
	},

	UpdateTitle: function(title, node, subnode, complement) {
		title.empty();

		//append node icons first
		if(!subnode && !complement) {
			//favorite toggle
			const favorite_toggle = document.createFullElement('img');
			Favorites.UpdateFavoriteImage(favorite_toggle, Favorites.IsFavorited(node));
			favorite_toggle.addEventListener('click', () => Favorites.Toggle(node));
			title.appendChild(favorite_toggle);

			//template save
			if(Templatables.includes(node.getEntity())) {
				const template_button = document.createFullElement('img', {src: 'images/disk.png', alt: 'Save', title: 'Save node as template'});
				//TODO send current node to function Templates.OpenSaveDialog
				template_button.addEventListener('click', () => Templates.OpenSaveDialog());
				title.appendChild(template_button);
			}
		}

		//append text
		title.appendChildren(generate_node_title_label(node));

		if(subnode) {
			title.appendChild(document.createTextNode(' > '));
			title.appendChildren(generate_node_title_label(subnode));
		}

		if(complement) {
			title.appendChild(document.createTextNode(' > '));
			title.appendChild(document.createTextNode(complement));
		}
	},

	ManageAutocomplete: function(input, results_container, filter, selection_callback, validation_callback) {
		//create results container if needed
		let results;
		if(results_container) {
			results = results_container;
		}
		else {
			results = document.createFullElement('ul', {'class': 'node_autocomplete', style: `width: ${input.offsetWidth}px;`});
			input.parentNode.appendChild(results);
		}

		//close results on a click outside
		document.addEventListener('click', function(event) {
			if(!results.contains(event.target) && !input.contains(event.target)) {
				results.style.display = 'none';
			}
		});

		let result_nodes;
		let result_node;

		function unselect_all() {
			result_node = undefined;
			results.querySelectorAll('li.node').forEach(function(item) {
				item.classList.remove('selected');
			});
		}

		function manage_mouse_over() {
			unselect_all();
			result_node = this.node;
			this.classList.add('selected');
		}

		function manage_mouse_out() {
			unselect_all();
			this.classList.remove('selected');
		}

		function manage_mouse_click() {
			results.style.display = 'none';
			input.value = '';
			validation_callback.call(undefined, this.node);
		}

		function manage_keys(event) {
			//enter
			if(event.key === 'Enter' && validation_callback && result_node) {
				validation_callback.call(undefined, result_node);
			}
			//escape
			if(event.key === 'Escape') {
				result_node = undefined;
				results.style.display = 'none';
			}
			//down or up
			if(event.key === 'ArrowUp' || event.key === 'ArrowDown') {
				//going down
				if(event.key === 'ArrowDown') {
					//initialize selection on the top node
					if(!result_node || result_node === result_nodes.last()) {
						result_node = result_nodes[0];
					}
					//normal case, select the next node
					else {
						result_node = result_nodes[result_nodes.indexOf(result_node) + 1];
					}
				}
				//going up
				else {
					//initialize selection on bottom node
					if(!result_node || result_node === result_nodes.first()) {
						result_node = result_nodes.last();
					}
					//normal case, select the previous node
					else {
						result_node = result_nodes[result_nodes.indexOf(result_node) - 1];
					}
				}
				//update results list
				results.querySelectorAll('li.node').forEach(function(item) {
					if(item.node === result_node) {
						item.classList.add('selected');
					}
					else {
						item.classList.remove('selected');
					}
				});
				//selection callback
				if(selection_callback) {
					selection_callback.call(undefined, result_node);
				}
			}
		}

		function draw_node(node, regexp) {
			const node_li = document.createFullElement('li', {'class': 'node'});
			node_li.node = node;
			//node label
			const node_label = document.createElement('span');
			node_label.innerHTML = node.getLocalizedLabel(Languages.GetLanguage()).replace(regexp, '<span class="highlight">$1</span>');
			node_li.appendChild(node_label);
			//node id
			if(node.id) {
				node_li.appendChild(document.createElement('br'));
				const node_id = document.createFullElement('span', {style: 'color: #5d5d5b; font-size: 8px;'});
				node_id.innerHTML = node.id.replace(regexp, '<span class="highlight">$1</span>');
				node_li.appendChild(node_id);
			}
			//add listeners
			node_li.addEventListener('mouseout', manage_mouse_out);
			node_li.addEventListener('mouseover', manage_mouse_over);
			node_li.addEventListener('click', manage_mouse_click);
			//return item
			return node_li;
		}

		function search_change() {
			//reset results as input content has changed
			results.empty();
			//reset selection
			result_node = undefined;
			//stop listening keyboard
			document.removeEventListener('keyup', manage_keys);

			const value = this.value;
			if(value) {
				//search node, filter them if needed and take only first 10 results
				let nodes = StudyHandler.GetStudy().search(value, Languages.GetLanguage());
				if(filter) {
					nodes = nodes.filter(filter);
				}
				nodes = nodes.slice(0, 10);
				//sort nodes according to entities
				const node_entities = {};
				nodes.forEach(node => {
					const entity_name = node.getEntity().name;
					if(!node_entities.hasOwnProperty(entity_name)) {
						node_entities[entity_name] = [];
					}
					node_entities[entity_name].push(node);
				});
				//rebuild result list sorted on entity
				result_nodes = [];
				//prepare regexp to highlight part of node matching the search
				const regexp_value = value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
				const regexp = new RegExp(`(${regexp_value})`, 'gi');
				for(const [entity_name, nodes] of Object.entries(node_entities)) {
					const entity = Entities[entity_name];
					//create entity section
					const entity_li = document.createFullElement('li', {'class': 'entity'});
					const entity_label = document.createFullElement('p');
					const entity_icon = entity.icon;
					const icon = Function.isFunction(entity_icon) ? entity_icon.call(undefined) : entity_icon;
					entity_label.appendChild(document.createFullElement('img', {src: `images/entities_icons/${icon}`, style: 'margin-left: 2px; margin-right: 2px;'}));
					entity_label.appendChild(document.createTextNode(entity.label));
					entity_li.appendChild(entity_label);
					const entity_ul = document.createFullElement('ul');
					entity_li.appendChild(entity_ul);
					//add nodes to entity section
					result_nodes.pushAll(nodes);
					nodes.map(n => draw_node(n, regexp)).forEach(Node.prototype.appendChild, entity_ul);
					results.appendChild(entity_li);
				}
				if(result_nodes.length > 0) {
					results.style.display = 'block';
					//listen keyboard in order to let user navigate through results
					document.addEventListener('keyup', manage_keys);
				}
			}
			else {
				results.style.display = 'none';
			}
		}

		//add listeners to show search results as user type
		input.addEventListener('input', search_change);
		//add listener on change to manage search when input content is cut or paste
		//input.addEventListener('change', search_change);
	}
};
