let current_menu;
let current_callback;

export const ContextualMenu = {
	Init: function() {
		document.addEventListener(
			'click',
			function(event) {
				//close menu with a click outside
				if(current_menu?.style.display === 'block' && !current_menu.contains(event.target)) {
					ContextualMenu.CloseMenu();
				}
			}
		);
	},
	OpenMenu: function(menu, event, onclose) {
		//close previous menu if any
		ContextualMenu.CloseMenu();
		//store current menu and callback
		current_menu = menu;
		current_callback = onclose;
		//position menu
		current_menu.style.position = 'fixed';
		current_menu.style.left = `${event.clientX}px`;
		if(event.clientY > document.body.clientHeight - 125) {
			current_menu.style.top = '';
			current_menu.style.bottom = `${document.body.clientHeight - event.clientY + 10}px`;
		}
		else {
			current_menu.style.bottom = '';
			current_menu.style.top = `${event.clientY}px`;
		}
		current_menu.style.display = 'block';
	},
	CloseMenu: function() {
		if(current_menu) {
			current_menu.style.display = 'none';
			if(current_callback) {
				current_callback();
			}
		}
	}
};
