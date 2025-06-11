export const Shortcuts = {
	Init: function() {
		document.getElementById('shortcuts_close').addEventListener('click', () => document.getElementById('shortcuts').close());
	},
	Open: function() {
		document.getElementById('shortcuts').showModal();
	}
};
