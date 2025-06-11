import {Effects} from '../tools/effects.js';
import {Languages} from '../languages.js';
import {FormStaticActions} from '../form_static_actions.js';
import {FormHelpers} from '../form_helpers.js';
import {CMSAction} from '../model/config/entities/cms_action.js';
import {Menu} from '../model/config/entities/menu.js';

const sort_margin = 5;

let selected_menu;

export default {
	form: 'edit_menu_form',
	init: function() {
		document.getElementById('edit_menu_form').addEventListener('submit', FormStaticActions.SubmitEditionForm);

		FormStaticActions.ManageLayoutEdition(document.getElementById('menu_layout_add'), document.getElementById('menu_layout_edit'), document.getElementById('menu_layout_delete'));

		Effects.Sortable(
			document.getElementById('menu_submenus'),
			function() {
				let offset = 0;
				this.children.forEach(function(child) {
					selected_menu.getSubmenu(child.dataset.submenuId).orderBy = offset;
					child.querySelector('span').textContent = offset;
					offset += sort_margin;
				});
			}
		);
	},
	open: function(menu) {
		selected_menu = menu;

		//add action to menu
		if(!menu.action) {
			const action = new CMSAction();
			menu.action = action;
		}
		FormHelpers.FillLocalizedInput(document.getElementById('menu_shortname'), menu.getStudy().languages);
		FormHelpers.FillLocalizedInput(document.getElementById('menu_longname'), menu.getStudy().languages);
		FormHelpers.FillLocalizedInput(document.getElementById('menu_description'), menu.getStudy().languages);
		FormHelpers.UpdateForm(document.getElementById('edit_menu_form'), menu);
		FormHelpers.EnhanceInputMapString(document.getElementById('menu_action_parameters'));

		FormStaticActions.UpdateLayoutEdition(menu, document.getElementById('menu_layout_add'), document.getElementById('menu_layout_edit'), document.getElementById('menu_layout_delete'));

		//draw interface used to sort submenus
		function draw_submenu(submenu) {
			const submenu_li = document.createFullElement('li', {'data-submenu-id': submenu.id});
			submenu_li.appendChild(document.createFullElement('img', {src: 'images/arrows_up_down.png', alt: 'Sort submenu', title: 'Sort submenu'}));
			submenu_li.appendChild(document.createFullElement('span', {}, submenu.orderBy === undefined ? 'x' : submenu.orderBy));
			submenu_li.appendChild(document.createTextNode(submenu.getLocalizedShortname(Languages.GetLanguage())));
			return submenu_li;
		}
		const submenus = menu.submenus.slice();
		submenus.sort(Menu.getOrderComparator());
		submenus.map(draw_submenu).forEach(Node.prototype.appendChild, document.getElementById('menu_submenus').empty());
	}
};
