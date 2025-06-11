import {UI} from './tools/ui.js';
import {ConfigHelpers} from './model_config.js';
import {StudyHandler} from './study_handler.js';
import {Router} from './router.js';
import {Favorites} from './favorites.js';
import {NodeClipboard} from './node_clipboard.js';
import {ContextualMenu} from './contextual_menu.js';
import {Entities} from './model/config/entities.js';

//TODO this piece of code is messy because it mixes general contextual menu and specific entity/node contextual menus
export const NodeContextualMenu = {
	Init: function() {

		const node_menu = document.getElementById('node_menu');
		const entity_menu = document.getElementById('entity_menu');

		//disable native context menu on custom context menus
		function disable_contextmenu(event) {
			event.preventDefault();
		}
		node_menu.addEventListener('contextmenu', disable_contextmenu);
		entity_menu.addEventListener('contextmenu', disable_contextmenu);

		document.getElementById('node_menu_favorite').addEventListener(
			'click',
			function() {
				Favorites.Add(StudyHandler.GetStudy().getNode(node_menu.dataset.nodeGlobalId));
				ContextualMenu.CloseMenu();
			}
		);

		document.getElementById('node_menu_delete').addEventListener(
			'click',
			function(event) {
				event.stop();
				const node = StudyHandler.GetStudy().getNode(node_menu.dataset.nodeGlobalId);
				const node_message = node.id ? `${node.getEntity().name} ${node.id}` : `this ${node.getEntity().name}`;
				ContextualMenu.CloseMenu();
				UI.Validate(`Are you sure you want to delete ${node_message}?`).then(confirmed => {
					if(confirmed) {
						node.delete();
					}
				});
			}
		);

		document.getElementById('node_menu_copy').addEventListener(
			'click',
			function() {
				NodeClipboard.CopyProgrammatically(StudyHandler.GetStudy().getNode(node_menu.dataset.nodeGlobalId));
				ContextualMenu.CloseMenu();
			}
		);

		function node_paste() {
			NodeClipboard.PasteProgrammatically(StudyHandler.GetStudy().getNode(node_menu.dataset.nodeGlobalId), true);
		}

		document.getElementById('node_menu_paste').addEventListener(
			'click',
			function() {
				node_paste.call(this);
				ContextualMenu.CloseMenu();
			}
		);

		document.getElementById('entity_menu_paste').addEventListener(
			'click',
			function() {
				node_paste.call(this);
				ContextualMenu.CloseMenu();
			}
		);

		document.getElementById('node_menu_clone').addEventListener(
			'click',
			function() {
				const node = StudyHandler.GetStudy().getNode(node_menu.dataset.nodeGlobalId);
				const parent = node.getParent();
				//find id
				let node_id = node.id;
				while(parent.getHasChild(node.getEntity(), undefined, node_id)) {
					node_id = `COPY_${node_id}`;
				}
				//duplicate node
				const new_node = ConfigHelpers.CloneNode(node, {id: node_id});
				//add node in parent
				parent.addChild(new_node);
				//select node in tree and in editor
				Router.SelectNode(new_node);
				UI.Notify(`New ${new_node.getEntity().name} created successfully`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
				//hide menu
				ContextualMenu.CloseMenu();
			}
		);
	},
	OpenNodeMenu: async function(event, node, onclose) {
		//delete
		if(node.getEntity() !== Entities.Study && !node.staticNode) {
			document.getElementById('node_menu_delete').removeAttribute('disabled');
		}
		else {
			document.getElementById('node_menu_delete').setAttribute('disabled', 'disabled');
		}
		//paste
		if(await NodeClipboard.IsPastable(node)) {
			document.getElementById('node_menu_paste').removeAttribute('disabled');
		}
		else {
			document.getElementById('node_menu_paste').setAttribute('disabled', 'disabled');
		}
		const menu = document.getElementById('node_menu');
		//set meta data
		menu.dataset.nodeGlobalId = node.getGlobalId();
		//open menu
		ContextualMenu.OpenMenu(menu, event, onclose);
	},
	OpenEntityMenu: async function(event, node, onclose) {
		//paste
		if(await NodeClipboard.IsPastable(node)) {
			document.getElementById('entity_menu_paste').removeAttribute('disabled');
		}
		else {
			document.getElementById('entity_menu_paste').setAttribute('disabled', 'disabled');
		}
		const menu = document.getElementById('entity_menu');
		//set meta data
		menu.dataset.nodeGlobalId = node.getGlobalId();
		//open menu
		ContextualMenu.OpenMenu(menu, event, onclose);
	}
};
