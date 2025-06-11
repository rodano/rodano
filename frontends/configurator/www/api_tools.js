import {UI} from './tools/ui.js';
import {API} from './api.js';

let api;
let user;
let two_step = false;

//create authenticate callback
let current_callback;
function interceptor(_, response, url, method, data, config) {
	return new Promise((resolve, reject) => {
		switch(response.status) {
			case 200:
			case 201:
				resolve(response.json());
				break;
			case 204:
				resolve();
				break;
			case 401:
				//ask for authentication except if error is explicitly managed (this is mandatory for login failure)
				//if there is an error during login (such as a bad password), it will loop forever if error is not explicitly managed
				//display login panel
				UI.StopLoading();
				/**@type {HTMLDialogElement}*/ (document.getElementById('authentication')).showModal();
				current_callback = () => api.request(url, method, data, config).then(resolve, reject);
				break;
			default:
				reject(response);
		}
	});
}

//authentication
export const APITools = {
	Init: function(api_url, bearer_token) {
		//create api
		api = new API(api_url, bearer_token, interceptor);
		APITools.API = api;

		const authentication_dialog = /**@type {HTMLDialogElement}*/ (document.getElementById('authentication'));

		//do not allow the user to close manually the dialog
		authentication_dialog.addEventListener('cancel', event => event.preventDefault());

		authentication_dialog.querySelector('form').addEventListener(
			'submit',
			function(event) {
				event.preventDefault();
				document.getElementById('authentication_credentials_error').style.display = 'none';
				document.getElementById('authentication_token_error').style.display = 'none';
				UI.StartLoading();
				api.auth.login(this['email'].value, this['password'].value, this['token'].value, undefined, undefined, false, {managed: true})
					.then(
						async response => {
							switch(response.status) {
								case 201: {
									const result = await response.json();
									api.bearerToken = result.token;
									//store new session id in URL
									const url = new URL(window.location.href);
									url.searchParams.set('bearer_token', result.token);
									window.history.pushState({}, undefined, url.href);
									authentication_dialog.close();
									//redo stored request
									if(current_callback) {
										current_callback.call();
									}
									break;
								}
								case 403: {
									if(two_step) {
										//show two step error
										document.getElementById('authentication_token_error').textContent = (await response.json()).response.message;
										document.getElementById('authentication_token_error').style.display = 'block';
									}
									else {
										two_step = true;
										//show token input
										document.getElementById('authentication_credentials').style.display = 'none';
										document.getElementById('authentication_token').style.display = 'block';
									}
									break;
								}
								case 429:
									//show credentials error
									document.getElementById('authentication_credentials_error').textContent = 'Too many failed requests. Use the main application to reset your password.';
									document.getElementById('authentication_credentials_error').style.display = 'block';
									break;
								default:
									//show credentials error
									document.getElementById('authentication_credentials_error').textContent = 'Wrong login or password';
									document.getElementById('authentication_credentials_error').style.display = 'block';
							}
						}
					).finally(UI.StopLoading);
			}
		);
	},
	RetrieveUser: function() {
		return api.users.me().then(result_user => user = result_user);
	},
	GetUser: function() {
		return user;
	}
};
