import {bus} from './model/config/entities_hooks.js';
import {bus_ui} from './bus_ui.js';
import {Router} from './router.js';

const hook_types = {
	onCreateNode: {
		label: 'On node created',
		example: `
if(node.getEntity().name === 'Attribute') {
	if(!node.workflowIds.includes('QUERY')) {
		node.workflowIds.push('QUERY');
		console.log('Workflow "QUERY" added automatically');
	}
}`,
	},
	onDeleteNode: {
		label: 'On node deleted',
		example: 'console.log(`You just deleted node "${node.id}"`);'
	}
};

const hooks = {};

function function_to_string(hook) {
	if(!hook) {
		return '';
	}
	const hook_text = hook.toString() ;
	return hook_text.substring(hook_text.indexOf('{') + 1, hook_text.lastIndexOf('}')).trim();
}

function draw_hook_type([id, description]) {
	const section = document.createElement('section');
	section.appendChild(document.createFullElement('h3', {}, description.label));
	const container = document.createElement('div');
	container.appendChild(document.createFullElement('textarea', {name: id}));
	container.appendChild(document.createFullElement('pre', {}, description.example.trim()));
	section.appendChild(container);
	return section;
}

function refresh_hook_values() {
	Object.keys(hook_types).forEach(h => document.querySelector('#hooks form')[h].value = function_to_string(hooks[h]));
}

export const Hooks = {
	Init: function() {
		const hooks_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('hooks'));

		//hooks dialog is managed by the application URL
		hooks_dialog.addEventListener('close', () => Router.CloseTool());

		const hooks_form = hooks_dialog.querySelector('form');
		hooks_form.addEventListener(
			'submit',
			function(event) {
				event.stop();
				const hooks_error = document.getElementById('hooks_error');
				hooks_error.style.display = 'none';
				try {
					hooks_form.querySelectorAll('textarea').forEach(function(textarea) {
						hooks[textarea.name] = textarea.value ? new Function('node', textarea.value) : undefined;
					});
					hooks_dialog.close();
				}
				catch(exception) {
					hooks_error.textContent = exception.message;
					hooks_error.style.display = 'block';
				}
			}
		);

		Object.entries(hook_types).map(draw_hook_type).forEach(Node.prototype.appendChild, document.getElementById('hooks_values'));

		bus_ui.register({
			onLoadStudy: function(event) {
				//restore study hooks
				if(event.settings?.hooks) {
					for(const [hook_id, hook_text] of Object.entries(event.settings.hooks)) {
						//rebuild hook function
						const args = hook_text.substring(hook_text.indexOf('(') + 1, hook_text.indexOf(')')).trim();
						const body = hook_text.substring(hook_text.indexOf('{') + 1, hook_text.lastIndexOf('}')).trim();
						//store hook
						hooks[hook_id] = new Function(args, body);
						//update ui
					}
				}
				//register hooks to configuration bus
				bus.register({
					onCreate: function(event) {
						if(hooks.onCreateNode) {
							hooks.onCreateNode(event.node);
						}
					},
					onDelete: function(event) {
						if(hooks.onDeleteNode) {
							hooks.onDeleteNode(event.node);
						}
					},
				});
			},
			onUnloadStudy: function(event) {
				//save study hooks
				event.settings.hooks = {};
				for(const hook_id in hooks) {
					if(hooks.hasOwnProperty(hook_id) && hooks[hook_id]) {
						event.settings.hooks[hook_id] = hooks[hook_id].toString();
					}
				}
			}
		});
	},
	Open: function() {
		refresh_hook_values();
		/**@type {HTMLDialogElement}*/ (document.getElementById('hooks')).showModal();
	}
};
