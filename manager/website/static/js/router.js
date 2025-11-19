import {UI} from './ui.js';
import {Hash} from './basic-tools/hash.js';
import {Control} from './control.js';
import {Reset} from './reset.js';
import {Restore} from './restore.js';
import {Backup} from './backup.js';

export const Router = {
	Init: function() {
		window.addEventListener(
			'hashchange',
			function() {
				//close open dialog if any
				document.querySelector('dialog[open]')?.close();
				//retrieve data encoded in hash
				const data = Hash.Decode(location.hash);
				if(data.hasOwnProperty('action')) {
					switch(data.action) {
						case 'control':
							Control.Launch(data.id);
							break;
						case 'backup':
							Backup.Launch();
							break;
						case 'reset':
							Reset.Launch();
							break;
						case 'restore':
							Restore.Launch();
							break;
						default:
							UI.Notify('Action unknown', {tag: 'error', icon: 'images/notifications/error.png', body: `There is no defined action ${data.action}`});
					}
				}
			}
		);
	},
	ForceRefresh: function() {
		const event = new UIEvent('hashchange', {bubbles: true, cancelable: true, detail: 1});
		window.dispatchEvent(event);
	}
};
