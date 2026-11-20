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

let started = false;

function start(token) {
	TokenManager.SetToken(token);
	//the token dialog is shown again when the token is rejected, the application must only be initialized once
	if(started) {
		ApplicationManager.Update();
		return;
	}
	started = true;

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

window.addEventListener(
	'load',
	function() {
		document.getElementById('token_form').addEventListener(
			'submit',
			function(event) {
				event.preventDefault();
				const token = this['token'].value;
				if(token) {
					if(this['save'].checked) {
						localStorage.setItem('token', token);
					}
					this.parentElement.close();
					start(token);
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

		start(token);
	}
);
