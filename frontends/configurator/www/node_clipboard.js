import {UI} from './tools/ui.js';
import {ConfigHelpers} from './model_config.js';
import {bus} from './model/config/entities_hooks.js';
import {Router} from './router.js';
import {Languages} from './languages.js';
import {StudyHandler} from './study_handler.js';
import {BusEventMoveNode} from './model/config/entities_config_bus.js';

const ACTION = {
	CUT: {
		name: 'CUT',
		label: node_label => `${node_label} cut successfully`
	},
	COPY: {
		name: 'COPY',
		label: node_label => `${node_label} copied successfully`
	}
};

let current_action;

function is_selection() {
	//find active element
	let active_element = document.activeElement;
	if(active_element.shadowRoot) {
		active_element = active_element.shadowRoot.activeElement;
	}
	//detect if there is any selected text
	const is_input_selection = active_element.selectionStart !== undefined && active_element.selectionEnd !== undefined && active_element.selectionStart !== active_element.selectionEnd;
	return is_input_selection || getSelection().toString().length > 0;
}

function check_node(clipboard_node, receiver_node) {
	return receiver_node.getEntity().children?.hasOwnProperty(clipboard_node.getEntity().name);
}

/*function generate_clipboard_item(node) {
	const node_id = new Blob([node.getGlobalId()], {type: 'text/plain'});
	//const node_id = new Blob([node.id], {type: MediaTypes.NODE_GLOBAL_ID});
	//const node_global_id = new Blob([node.getGlobalId()], {type: MediaTypes.NODE_GLOBAL_ID});
	//const node_data = new Blob([JSON.stringify(node, undefined, '\t')], {type: 'application/json'});
	return new ClipboardItem({
		[node_id.type]: node_id
	});
}*/

function cut_copy_listener(action) {
	return async event => {
		//do nothing if there is a native selection, let the native copy behavior happen
		if(is_selection()) {
			return;
		}
		if(Router.selectedNode) {
			//fill event (old API)
			//it can still be used to handle copy/paste made using the keyboard and allows to fine-tune the data put in the clipboard
			//BUG it's not possible to use old and new API at the same time
			//using method "navigator.clipboard.writeText" breaks "event.clipboardData.setData"
			//NodeTools.FillDataTransfer(event.clipboardData, Router.selectedNode);
			//event.clipboardData.setData(MediaTypes.CLIPBOARD_ACTION, action.name);
			//fill clipboard (new API)
			//it's the only way to read the content of the clipboard outside of an event (like in method "IsPastable")
			await navigator.clipboard.writeText(Router.selectedNode.getGlobalId());
			current_action = action;
			UI.Notify(action.label(Router.selectedNode.getLocalizedLabel(Languages.GetLanguage())), {
				tag: 'info',
				icon: 'images/notifications_icons/done.svg',
				body: 'Paste it wherever you need.'
			});
			//do not copy ancestor nodes
			event.stopPropagation();
			event.preventDefault();
		}
	};
}

function paste(clipboard_node, receiver_node, flexible, source_action) {
	const language = Languages.GetLanguage();
	let parent = receiver_node;
	//check if node can be pasted in the parameter node
	if(!check_node(clipboard_node, parent)) {
		let error;
		//if flexible, check if node can be pasted in the parent of the parameter node to allow paste among siblings
		if(flexible) {
			parent = parent.getParent();
			if(!check_node(clipboard_node, parent)) {
				error = `Unable to paste ${clipboard_node.getLocalizedLabel(language)} along ${parent.getLocalizedLabel(language)}`;
			}
		}
		else {
			error = `Unable to paste ${clipboard_node.getLocalizedLabel(language)} in ${parent.getLocalizedLabel(language)}`;
		}
		if(error) {
			UI.Notify(error, {
				tag: 'error',
				icon: 'images/notifications_icons/warning.svg',
				body: `Entity ${clipboard_node.getEntity().name} is not a child of entity ${parent.getEntity().name}.`
			});
			return;
		}
	}
	//move node for a cut
	if(source_action === ACTION.CUT) {
		//check that a node with the same id does not already exist in new parent
		if(!parent.getHasChild(clipboard_node.getEntity(), undefined, clipboard_node.id)) {
			//retrieve clipboard parent node
			const clipboard_node_parent = clipboard_node.getParent();
			//update model
			bus.dispatch(new BusEventMoveNode(clipboard_node.getEntity().name, clipboard_node, clipboard_node_parent, receiver_node));
			UI.Notify(`${clipboard_node.getEntity().name} moved successfully`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
		}
		else {
			UI.Notify(`Unable to past ${clipboard_node.getEntity().name} here because a node with the same id already exists`, {
				tag: 'error',
				icon: 'images/notifications_icons/warning.svg'
			});
		}
	}
	//duplicate node otherwise
	else {
		//find id
		let node_id = clipboard_node.id;
		while(parent.getHasChild(clipboard_node.getEntity(), undefined, node_id)) {
			node_id = `COPY_${node_id}`;
		}
		const new_node = ConfigHelpers.CloneNode(clipboard_node, {id: node_id});
		//add node in parent
		parent.addChild(new_node);
		//select node in tree and in editor
		Router.SelectNode(new_node);
		UI.Notify(`${new_node.getEntity().name} cloned successfully`, {tag: 'info', icon: 'images/notifications_icons/done.svg'});
	}
}

export const NodeClipboard = {
	Init: function() {
		//add listeners
		document.addEventListener('copy', cut_copy_listener(ACTION.COPY));
		document.addEventListener('cut', cut_copy_listener(ACTION.CUT));
		document.addEventListener('paste', async event => {
			//use old API because it's possible as we have a clipboard event
			//this API is more reliable because we have full control over the data in the clipboard
			//do not use the new API, because it could be dangerous
			//there is no other choice at the miment
			//if a user has any global id that he may have copied from text, the node will be pasted
			//TODO clipboard is not supported by Firefox yet (see here https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/readText)
			if(navigator.clipboard.readText) {
				const node_global_id = await navigator.clipboard.readText();
				if(!node_global_id) {
					return;
				}
				try {
					const clipboard_node = StudyHandler.GetStudy().getNode(node_global_id);
					//retrieve action
					//this does not work because clipboard data is empty
					//const source_action = ACTION[event.clipboardData.getData(MediaTypes.CLIPBOARD_ACTION)];
					const source_action = current_action;
					paste(clipboard_node, Router.selectedNode, true, source_action);
					//do not try to paste in ancestor nodes and prevent a paste from system clipboard
					event.stopPropagation();
					event.preventDefault();
				}
				catch(error) {
					//multiple error could happen
					//text in clipboard could be anything different thant a global id
					//or it could be a global id but the node no longer exist or is from an other study
					console.error(`Unable to paste node ${error.message}`);
				}
			}
		});
	},
	CopyProgrammatically: async function(node) {
		//VERSION 2
		//BUG some browsers do not support clipboard item yet
		await navigator.clipboard.writeText(node.getGlobalId());
		current_action = ACTION.COPY;
		UI.Notify('Node copied in clipboard', {
			tag: 'info',
			icon: 'images/notifications_icons/done.svg',
			body: 'Paste it wherever you need.'
		});
		//VERSION 1.5
		//document.execCommand('copy');
		//VERSION 1
		//this is the old way to do but this uses a different clipboard
		//event triggered programmatically cannot interact with the real system clipboard
		/*const transfer = new DataTransfer();
		NodeTools.FillDataTransfer(transfer, node);
		transfer.setData(MediaType.CLIPBOARD_ACTION, 'copy');
		const event = new ClipboardEvent('copy', {clipboardData: transfer});
		window.dispatchEvent(event);
		UI.Notify(`"${node.getLocalizedLabel(Languages.GetLanguage())}" copied`, {
			tag: 'info',
			icon: 'images/notifications_icons/done.svg',
			body: 'Paste it wherever you need.'
		});*/
	},
	//method that check if clipboard node can be pasted in the parameter node
	//flexible boolean can be set to true to check if clipboard node can be pasted in the parent of the parameter node, allowing to paste a node among its siblings
	IsPastable: async function(receiver_node) {
		//TODO clipboard is not supported by Firefox yet (see here https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/readText)
		if(navigator.clipboard.readText) {
			//retrieve clipboard node
			try {
				const node_global_id = await navigator.clipboard.readText();
				if(node_global_id) {
					try {
						const clipboard_node = StudyHandler.GetStudy().getNode(node_global_id);
						//check if node can be pasted in the parameter node
						if(check_node(clipboard_node, receiver_node)) {
							return true;
						}
						//if node can be pasted in the parent of the parameter node
						return receiver_node.hasParent() && check_node(clipboard_node, receiver_node.getParent());
					}
					catch(error) {
						//multiple error could happen
						//text in clipboard could be anything different thant a global id
						//or it could be a global id but the node no longer exist or is from an other study
						console.error(`Unable to check if node can be pasted: ${error.message}`);
					}
				}
			}
			catch(error) {
				console.error(`Unable to read clipboard: ${error.message}`);
			}
		}
		return false;
	},
	PasteProgrammatically: async function(receiver_node, flexible) {
		//TODO clipboard is not supported by Firefox yet (see here https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/readText)
		if(navigator.clipboard.readText) {
			const node_global_id = await navigator.clipboard.readText();
			if(node_global_id) {
				try {
					const clipboard_node = StudyHandler.GetStudy().getNode(node_global_id);
					paste(clipboard_node, receiver_node, flexible);
				}
				catch(error) {
					//multiple error could happen
					//text in clipboard could be anything different thant a global id
					//or it could be a global id but the node no longer exist or is from an other study
					console.error(`Unable to paste node: ${error.message}`);
				}
			}
		}
	}
};
