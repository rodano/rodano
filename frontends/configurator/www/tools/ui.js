import '../basic-tools/extension.js';

const UI = {};

UI.StartLoading = function() {
	document.body.classList.add('loading');
	document.getElementById('loading').style.display = 'block';
};

UI.StopLoading = function() {
	document.body.classList.remove('loading');
	document.getElementById('loading').style.display = 'none';
};

UI.IsDialogOpen = function() {
	return !document.querySelectorAll('dialog[open]').isEmpty();
};

UI.CloseDialogs = function() {
	document.querySelectorAll('dialog[open]').forEach(d => d.close());
};

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

	UI.Notify = function(message, options) {
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
				notification_icon.style.display = 'block';
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

(function() {
	const modals = [];
	//while a modal is being open, disable the code that closes modals (this modal and the previous ones) when a click happens elsewhere
	//if the modal is open inside a click listener, the modal must not be immediately closed by the same click event that opened it
	let being_open;

	//close modal with click outside modal
	function click_close_modal(event) {
		//do not close the modal or stacked modals if one is currently being open
		if(!being_open && !modals.isEmpty()) {
			const modal = modals.last();
			if(!modal.locked && !modal.contains(event.target)) {
				UI.CloseModal(modal);
			}
		}
	}

	//close modal windows with escape key
	function escape_close_modal(event) {
		if(event.key === 'Escape' && !modals.isEmpty()) {
			const modal = modals.last();
			if(!modal.locked) {
				UI.CloseModal(modal);
			}
		}
	}

	UI.OpenModal = function(element, locked) {
		//store locking status
		element.locked = locked || false;

		//add new modal to list
		modals.push(element);

		const overlay = document.getElementById('modal_overlay');

		//show overlay if this is the first modal to open
		if(modals.length === 1) {
			overlay.style.display = 'block';
		}

		//put modal window just over overlay
		const index = parseInt(overlay.style.zIndex) || 100;
		overlay.style.zIndex = (index + 2).toString();
		element.style.zIndex = (index + 3).toString();
		element.style.display = 'flex';

		//add document listeners for first modal
		if(modals.length === 1) {
			document.addEventListener('keydown', escape_close_modal);
			document.addEventListener('click', click_close_modal);
		}

		//disable click listener while the modal is being open
		//restore it afterwards, in the next browser tick
		being_open = true;
		setTimeout(() => being_open = false, 1);
	};

	UI.CloseModal = function(element) {
		//retrieve modal
		const modal = element || modals.last();
		if(modal) {
			//hide modal window
			modal.style.display = 'none';

			const overlay = document.getElementById('modal_overlay');

			//remove document listener for last modal
			if(modals.length === 1) {
				document.removeEventListener('click', click_close_modal);
				document.removeEventListener('keydown', escape_close_modal);
			}

			//put overlay just under modal window
			const index = parseInt(overlay.style.zIndex);
			overlay.style.zIndex = (index - 2).toString();

			if(modals.length === 1) {
				//remove overlay if this is last open modal
				overlay.style.display = 'none';
			}

			//remove modal from list
			modals.removeElement(modal);
		}
	};

	UI.CloseModals = function() {
		modals.slice().forEach(UI.CloseModal);
	};

	UI.IsModalOpen = function() {
		return !modals.isEmpty();
	};
})();

(function() {
	//show tab associated content and hide other contents
	function select_tab() {
		if(!this.classList.contains('disabled')) {
			this.parentNode.children.forEach(function(tab) {
				if(tab === this) {
					tab.classList.add('selected');
					document.getElementById(tab.dataset.tab).style.display = 'block';
				}
				else {
					tab.classList.remove('selected');
					document.getElementById(tab.dataset.tab).style.display = 'none';
				}
			}, this);
		}
	}

	UI.Tabify = function(container) {
		container.children.forEach(function(tab) {
			document.getElementById(tab.dataset.tab).style.display = tab.classList.contains('selected') ? 'block' : 'none';
			tab.addEventListener('click', select_tab);
		});
	};
})();

//use a native dialog HTML element instead of a modal
UI.Validate = function(message, yes_text, no_text) {
	const validate_window = /**@type {HTMLDialogElement}*/ (document.getElementById('validate'));
	//message can either be a string, or a HTML element
	const validate_message = document.getElementById('validate_message');
	validate_message.empty();
	if(String.isString(message)) {
		validate_message.textContent = message;
	}
	else {
		validate_message.appendChild(message);
	}
	//manage buttons
	const validate_buttons = document.getElementById('validate_buttons');
	validate_buttons.empty();

	return new Promise(resolve => {
		const no_button_container = document.createElement('li');
		const no_button = document.createFullElement(
			'button',
			{type: 'button', autofocus: true},
			no_text || 'No',
			{
				click: function(event) {
					event.stop();
					validate_window.close();
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
					validate_window.close();
					resolve(true);
				}
			}
		);
		no_button_container.appendChild(no_button);
		yes_button_container.appendChild(yes_button);
		validate_buttons.appendChild(no_button_container);
		validate_buttons.appendChild(yes_button_container);

		validate_window.showModal();
		no_button.focus();
	});
};

//delay task to let browser time to repaint
UI.Delay = function(callback) {
	setTimeout(callback, 50);
};

export {UI};
