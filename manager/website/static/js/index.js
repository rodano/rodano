import './basic-tools/extension.js';
import './basic-tools/dom_extension.js';

import {ApplicationManager} from './application.js';
import {Control} from './control.js';
import {Reset} from './reset.js';
import {Restore} from './restore.js';
import {Backup} from './backup.js';
import {Messaging} from './messaging.js';
import {Router} from './router.js';
import {Tasks} from './tasks.js';
import {Users} from './users.js';
import {TokenManager} from './token.js';

window.addEventListener(
	'load',
	function() {
		document.getElementById('token_form').addEventListener(
			'submit',
			function(event) {
				event.preventDefault();
				const token = this['token'].value;
				if(token) {
					TokenManager.SetToken(token);
					if(this['save'].checked) {
						localStorage.setItem('token', token);
					}
					this.parentElement.close();
					ApplicationManager.Update();
				}
			}
		);

		document.getElementById('error_close').addEventListener(
			'click',
			function() {
				this.closest('dialog').close();
			}
		);

		const token = localStorage.getItem('token');
		if(!token) {
			document.getElementById('token').showModal();
			return;
		}

		TokenManager.SetToken(token);

		//initialize everything that does not require application info
		Messaging.Init();
		Reset.Init();
		Restore.Init();
		Backup.Init();
		Tasks.Init();
		Users.Init();
		ApplicationManager.Init();
		Control.Init();
		Router.Init();
		Router.ForceRefresh();
	}
);
