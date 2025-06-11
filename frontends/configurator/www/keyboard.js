import {UI} from './tools/ui.js';
import {Router} from './router.js';
import {StudyHandler} from './study_handler.js';
import {Configuration} from './configuration.js';
import {ConfigurationSerialized} from './configuration_serialized.js';
import {Backups} from './backups.js';
import {Logs} from './logs.js';
import {Search} from './search.js';
import {Transfer} from './transfer.js';

//allowed delay between two keys of a sequence
const delay = 500;

let sequence = [];
let last_press = undefined;

function manage_sequence(sequence) {
	//shortcuts require a study
	if(StudyHandler.HasStudy()) {
		//put focus on search field with s
		if(Object.equals(sequence, ['s'])) {
			Search.Focus();
			return true;
		}
		//open log panel with o+l
		if(Object.equals(sequence, ['o', 'l'])) {
			Logs.OpenDialog();
			return true;
		}
		//open transfer window with o+t
		if(Object.equals(sequence, ['o', 't'])) {
			Transfer.Open();
			return true;
		}
		//close current configuration with c+w
		if(Object.equals(sequence, ['c', 'w'])) {
			Configuration.Close();
			return true;
		}
		//push to server with c+p
		if(Object.equals(sequence, ['c', 'p'])) {
			Configuration.PushToServer();
			return true;
		}
		//download current configuration in json with c+d
		if(Object.equals(sequence, ['c', 'd'])) {
			Configuration.Download();
			return true;
		}
		//shortcuts that require no modal to be already open
		if(!UI.IsDialogOpen()) {
			//view current configuration in json with c+v
			if(Object.equals(sequence, ['c', 'v'])) {
				ConfigurationSerialized.Show();
				return true;
			}
			//save locally with c+s
			if(Object.equals(sequence, ['c', 's'])) {
				Backups.OpenSaveDialog();
				return true;
			}
			//load locally with c+o
			if(Object.equals(sequence, ['c', 'o'])) {
				Backups.OpenLoadDialog();
				return true;
			}
		}
	}
	return false;
}

function manage_one(event) {
	//save with ctrl+s keys
	if(event.ctrlKey && event.key.toLowerCase() === 's' && Router.selectedNode) {
		//do not save when a modal is opened
		if(!UI.IsDialogOpen()) {
			//node
			if(Router.selectedNode && !Router.selectedNode.staticNode) {
				const editor = document.getElementById(`edit_${Router.selectedNode.getEntity().id}`);
				if(editor.style.display !== 'none') {
					//remove focus from active element to trigger pending onchange events on input fields
					document.activeElement.blur();
					//add focus to submit button
					const form = editor.querySelector('form');
					/**@type {HTMLButtonElement}*/
					const button = form.querySelector('button:not([disabled]):not([type="button"])');
					button.focus();
					//trigger submit if form is valid
					if(form.checkValidity()) {
						const event = new SubmitEvent('submit', {bubbles: true, cancelable: true, submitter: button});
						form.dispatchEvent(event);
					}
					else {
						//retrieve validation messages
						let error = '';
						form.elements.forEach(function(element) {
							if(element.validationMessage) {
								if(error) {
									error += '\n';
								}
								error += (`Field ${element.name}: ${element.validationMessage.toLowerCase()}`);
							}
						});
						UI.Notify('Correct errors before saving', {tag: 'error', icon: 'images/notifications_icons/warning.svg', body: error});
					}
				}
				return true;
			}
			//rule
			/*else if(selected_conditions) {
				event.stop();
				//trigger submit
				const event = document.createEvent('UIEvent');
				event.initUIEvent('submit', true, true, window, 1);
				document.getElementById('rule').dispatchEvent(event);
			}*/
		}
	}
	//create new node with ctrl+n keys
	//BUG chrome does not allow ctrl+n http://code.google.com/p/chromium/issues/detail?id=33056
	/*if(event.ctrlKey && event.key.toLowerCase() === 'n') {
		event.preventDefault();
		event.stopPropagation();
		add_node();
	}*/

	return false;
}

export const Keyboard = {
	Init: function() {
		document.addEventListener(
			'keydown',
			function(event) {
				//manage one key shortcut
				if(manage_one(event)) {
					event.stop();
					return;
				}
				//manage multiple keys shortcut
				//find active element in page or in shadow dom if document active element is a contain a shadow root
				let active_element = document.activeElement;
				if(active_element.shadowRoot) {
					active_element = active_element.shadowRoot.activeElement;
				}
				const tag = active_element.tagName;
				const is_typing = ['INPUT', 'SELECT', 'TEXTAREA'].includes(tag) || active_element instanceof HTMLElement && active_element.isContentEditable;
				const is_character = /^[a-z]$/.test(event.key);
				//do nothing if something is currently edited, or if a meta key or a non character is pressed
				if(!is_typing && !event.ctrlKey && !event.shiftKey && is_character) {
					event.stop();
					const now = Date.now();
					//reset sequence if last key press is too much time ago
					if(last_press && (now - last_press) > delay) {
						sequence = [];
					}
					last_press = now;
					sequence.push(event.key.toLowerCase());
					if(manage_sequence(sequence)) {
						sequence = [];
					}
				}
				else {
					sequence = [];
				}
			}
		);
	}
};
