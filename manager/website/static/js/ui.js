import './basic-tools/extension.js';

const UI = {};

(function() {
	const notification_close_time = 5000;
	let notification_timeout;

	window.addEventListener('load', function() {
		//manage non native notifications
		function hide() {
			this.style.display = 'none';
		}
		const notification = document.getElementById('notification');
		notification.addEventListener('click', hide);
		//TODO clean this mess as soon as browsers support good event
		notification.addEventListener('animationend', hide);
		notification.addEventListener('webkitAnimationEnd', hide);
	});

	//remember if notification permission has been requested to avoid asking to the user more than once
	let notification_permission_requested = false;

	UI.Notify = function(message, options = {}) {
		//ask for permission if user has not explicitly denied nor granted notification (permission can be default or undefined)
		if(!['granted', 'denied'].includes(Notification.permission) && !notification_permission_requested) {
			notification_permission_requested = true;
			Notification.requestPermission(function() {
				//re-notify
				UI.Notify(message, options);
			});
		}
		//use native notification
		else if(Notification.permission === 'granted') {
			const enhanced_options = Object.assign({
				lang: 'EN',
				silent: true
			}, options);
			const notification = new Notification(message, enhanced_options);
			if(notification_timeout) {
				clearTimeout(notification_timeout);
			}
			notification.addEventListener('show', function() {
				notification_timeout = setTimeout(function() {
					notification.close();
				}, notification_close_time);
			});
		}
		//fallback on html notification
		else {
			//update icon
			const notification_icon = /**@type HTMLImageElement */ (document.getElementById('notification_icon'));
			if(options.hasOwnProperty('icon')) {
				notification_icon.src = options.icon;
				notification_icon.style.display = 'inline';
			}
			else {
				notification_icon.style.display = 'none';
			}
			//update title
			const notification_title = document.getElementById('notification_title');
			notification_title.textContent = message;
			//update body
			const notification_body = document.getElementById('notification_body');
			if(options.hasOwnProperty('body')) {
				notification_body.textContent = options.body;
				notification_body.style.display = 'block';
			}
			else {
				notification_body.style.display = 'none';
			}

			//manage display of animation
			const notification = document.getElementById('notification');
			if(notification_timeout) {
				clearTimeout(notification_timeout);
			}
			//update notification
			notification.classList.remove('fadeout');
			notification.style.display = 'block';
			//add class that will start animation
			notification_timeout = setTimeout(function() {
				notification.classList.add('fadeout');
			}, notification_close_time);
		}
	};
})();

UI.Confirm = function(message, yes_text, no_text) {
	const confirm_window = /**@type {HTMLDialogElement}*/ (document.getElementById('confirm'));
	//message can either be a string, or a HTML element
	const confirm_message = document.getElementById('confirm_message');
	confirm_message.empty();
	if(String.isString(message)) {
		confirm_message.textContent = message;
	}
	else {
		confirm_message.appendChild(message);
	}
	//manage buttons
	const confirm_buttons = document.getElementById('confirm_buttons');
	confirm_buttons.empty();

	return new Promise(resolve => {
		const no_button_container = document.createElement('li');
		const no_button = document.createFullElement(
			'button',
			{type: 'button', autofocus: true},
			no_text || 'No',
			{
				click: function(event) {
					event.stop();
					confirm_window.close();
					resolve(false);
				}
			}
		);
		const yes_button_container = document.createElement('li');
		const yes_button = document.createFullElement(
			'button',
			{type: 'button', style: 'margin-left: 0.5rem;'},
			yes_text || 'Yes',
			{
				click: function(event) {
					event.stop();
					confirm_window.close();
					resolve(true);
				}
			}
		);
		no_button_container.appendChild(no_button);
		yes_button_container.appendChild(yes_button);
		confirm_buttons.appendChild(no_button_container);
		confirm_buttons.appendChild(yes_button_container);

		confirm_window.showModal();
		no_button.focus();
	});
};

export {UI};
