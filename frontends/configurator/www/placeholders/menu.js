import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {Menu} from '../model/config/entities/menu.js';

const sort_margin = 5;

let selected_node;

Effects.Sortable(
	document.getElementById('nodes_menus_sort'),
	function() {
		let offset = 0;
		this.children.forEach(function(child) {
			selected_node.getMenu(child.dataset.menuId).orderBy = offset;
			child.querySelector('span').textContent = offset;
			offset += sort_margin;
		});
	}
);

function draw_menu(menu) {
	const menu_li = document.createFullElement('li', {'data-menu-id': menu.id});
	menu_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort menu', title: 'Sort menu'}));
	menu_li.appendChild(document.createFullElement('span', {}, menu.orderBy === undefined ? 'x' : menu.orderBy));
	menu_li.appendChild(document.createTextNode(menu.getLocalizedLabel(Languages.GetLanguage())));
	return menu_li;
}

export default {
	open: function(node) {
		selected_node = node;

		const menus = node.menus.slice();
		menus.sort(Menu.getOrderComparator());
		menus.map(draw_menu).forEach(Node.prototype.appendChild, document.getElementById('nodes_menus_sort').empty());
	}
};
