import {Settings} from './settings.js';
import {Languages} from './languages.js';
import {Config} from './model_config.js';
import {bus_ui} from './bus_ui.js';
import {Configuration} from './configuration.js';
import {ConfigurationSerialized} from './configuration_serialized.js';
import {Logs} from './logs.js';
import {Backups} from './backups.js';
import {Shortcuts} from './shortcuts.js';
import {Statistics} from './statistics.js';
import {Wizards} from './wizards.js';
import {Matrices} from './matrices.js';
import {Digests} from './digests.js';
import {Themes} from './themes.js';
import {Entities} from './model/config/entities.js';
import {Assignables, Attributables, RightAssignables} from './model/config/entities_categories.js';

//listeners for menu items (a and button elements)
function add_top_item_listeners(items) {
	items.forEach(i => {
		i.addEventListener('mouseover', mouseover_item);
		i.addEventListener('mousedown', mousedown_item);
	});
}

function add_item_listeners(items) {
	items.forEach(i => {
		i.addEventListener('mouseover', mouseover_item);
		i.addEventListener('click', click_item);
	});
}

//a "click" on a open (focused) top menu item must close the menu (by releasing the focus)
//when clicking a focusable element, the activeElement is set before the click listener is executed on this element
//when the click is caught, the current element is already the active element
//so there is no way to know if this click ocurred on an opened or a closed element
//that's why the listener must be attached to the mousedown event instead of the click event
//also, this must be executed with a delay
//otherwise when an closing mousedown occurs the menu won't close because it will be re-open by the focus
//even if the activeElement is set before anything else, the focus is set only after the click
function mousedown_item() {
	if(this === document.activeElement) {
		setTimeout(() => this.blur());
	}
}

//a click on non top menu item must close the menu (by releasing the focus)
function click_item() {
	this.blur();
}

//put focus on the item if the focus is already on the menu bar
function mouseover_item() {
	if(document.getElementById('menubar').contains(document.activeElement)) {
		this.focus();
	}
}

function open_menu(menu) {
	menu.querySelector('button, a').focus();
}

function menu_item_is_disabled(menu) {
	return menu.firstElementChild.hasAttribute('disabled');
}

function find_first_enabled_child(menu) {
	const sub_menu = menu.lastElementChild.firstElementChild;
	return menu_item_is_disabled(sub_menu) ? find_next_enabled_siblings(sub_menu) : sub_menu;
}

function find_last_enabled_child(menu) {
	const sub_menu = menu.lastElementChild.lastElementChild;
	return menu_item_is_disabled(sub_menu) ? find_next_enabled_siblings(sub_menu) : sub_menu;
}

function find_next_enabled_siblings(menu) {
	let next_menu = menu;
	do {
		if(next_menu.nextElementSibling) {
			next_menu = next_menu.nextElementSibling;
		}
		else {
			next_menu = next_menu.parentNode.firstElementChild;
		}
	} while(menu_item_is_disabled(next_menu));
	return next_menu;
}

function find_previous_enabled_siblings(menu) {
	let next_menu = menu;
	do {
		if(next_menu.previousElementSibling) {
			next_menu = next_menu.previousElementSibling;
		}
		else {
			next_menu = next_menu.parentNode.lastElementChild;
		}
	} while(menu_item_is_disabled(next_menu));
	return next_menu;
}

export const Menu = {
	Init: function() {
		const menubar = document.getElementById('menubar');

		//manage keyboard to navigate in menu
		menubar.addEventListener(
			'keydown',
			function(event) {
				const selected_menu = document.activeElement.parentElement;
				if(menubar.contains(selected_menu)) {
					switch(event.key) {
						case 'ArrowDown':
							//top level menu
							if(menubar.children.includes(selected_menu)) {
								open_menu(find_first_enabled_child(selected_menu));
							}
							//second level menu
							else {
								open_menu(find_next_enabled_siblings(selected_menu));
							}
							event.stop();
							break;
						case 'ArrowUp':
							//top level menu
							if(menubar.children.includes(selected_menu)) {
								open_menu(find_last_enabled_child(selected_menu));
							}
							//second level menu
							else {
								open_menu(find_previous_enabled_siblings(selected_menu));
							}
							event.stop();
							break;
						case 'ArrowLeft':
							//third level menu
							if(selected_menu.parentElement.parentElement.classList.contains('expendable')) {
								selected_menu.parentElement.style.display = 'none';
								open_menu(selected_menu.parentElement.parentElement);
							}
							//retrieve parent menu
							else {
								const parent_menu = menubar.children.includes(selected_menu) ? selected_menu : selected_menu.parentElement.parentElement;
								if(parent_menu.previousElementSibling) {
									open_menu(parent_menu.previousElementSibling);
								}
							}
							event.stop();
							break;
						case 'ArrowRight':
							//third level menu
							if(selected_menu.classList.contains('expendable')) {
								selected_menu.lastElementChild.style.display = 'block';
								open_menu(selected_menu.lastElementChild.firstElementChild);
							}
							//retrieve parent menu
							else {
								const parent_menu = menubar.children.includes(selected_menu) ? selected_menu : selected_menu.parentElement.parentElement;
								if(parent_menu.nextElementSibling) {
									open_menu(parent_menu.nextElementSibling);
								}
							}
							event.stop();
							break;
					}
				}
			}
		);

		document.addEventListener(
			'keyup',
			function(event) {
				//put focus on first menu with left alt key
				if(event.key === 'Alt' && event.location === KeyboardEvent.DOM_KEY_LOCATION_LEFT) {
					menubar.querySelector('button, a').focus();
					event.preventDefault();
				}
				//remove focus from menu with escape key
				if(event.key === 'Escape' && menubar.contains(document.activeElement)) {
					document.activeElement.blur();
				}
			}
		);

		document.addEventListener(
			'click',
			function(event) {
				if(!menubar.contains(event.target) && menubar.contains(document.activeElement)) {
					document.activeElement.blur();
				}
			}
		);

		//settings menus
		function toggle_setting() {
			const setting_id = this.dataset.settingId;
			Settings.Set(setting_id, !Settings.Get(setting_id));
		}
		document.querySelectorAll('#menubar button[data-setting-id]').forEach(function(menu) {
			menu.firstElementChild.src = Settings.Get(menu.dataset.settingId) ? 'images/tick.png' : 'images/untick.png';
			menu.addEventListener('click', toggle_setting);
		});

		//themes menu
		function menu_theme_listener() {
			Themes.SelectTheme(this.dataset.themeId);
			menu_themes.children.forEach(menu_theme => {
				menu_theme.firstElementChild.firstElementChild.src = menu_theme === this.parentNode ? 'images/tick.png' : 'images/untick.png';
			});
		}

		function draw_menu_theme(theme) {
			const menu_theme_li = document.createFullElement('li');
			const menu_theme_button = document.createFullElement('button', {tabindex: '0', 'data-theme-id': theme});
			menu_theme_button.addEventListener('click', menu_theme_listener);
			menu_theme_button.appendChild(document.createFullElement('img', {src: theme === Themes.GetSelectedTheme() ? 'images/tick.png' : 'images/untick.png'}));
			menu_theme_button.appendChild(document.createTextNode(theme));
			menu_theme_li.appendChild(menu_theme_button);
			return menu_theme_li;
		}

		const menu_themes = document.getElementById('menu_themes');
		Themes.GetThemes().map(draw_menu_theme).forEach(Node.prototype.appendChild, menu_themes.empty());

		//matrices menu
		function draw_menu_matrix(entity) {
			const menu_profile_li = document.createFullElement('li');
			//matrix link
			const menu_profile_li_a = document.createFullElement('a', {tabindex: '0', href: `#matrix=profile&entity=${entity.name}`});
			const entity_icon = entity.icon;
			const icon = Function.isFunction(entity_icon) ? entity_icon.call() : entity_icon;
			menu_profile_li_a.appendChild(document.createFullElement('img', {src: `images/entities_icons/${icon}`}));
			menu_profile_li_a.appendChild(document.createTextNode(entity.label));
			//matrix item
			menu_profile_li.appendChild(menu_profile_li_a);
			return menu_profile_li;
		}

		const matrices = [];
		matrices.pushAll(Assignables);
		matrices.pushAll(RightAssignables);
		matrices.pushAll(Attributables.filter(a => Entities.Study.children.hasOwnProperty(a.name)));
		matrices.map(draw_menu_matrix).forEach(Node.prototype.appendChild, document.getElementById('menu_matrices').empty());

		//triggers menu
		const menu_triggers = document.getElementById('menu_triggers');
		for(const [trigger_id, trigger] of Object.entries(Config.Enums.Trigger)) {
			const menu_trigger_li = document.createFullElement('li');
			//trigger link
			const menu_trigger_li_a = document.createFullElement('a', {tabindex: '0', href: `#trigger=${trigger_id}`, 'class': 'naked'}, trigger.shortname['en']);
			menu_trigger_li.appendChild(menu_trigger_li_a);
			menu_triggers.appendChild(menu_trigger_li);
		}

		//other menus
		document.getElementById('menu_template_push').addEventListener('click', () => Configuration.PushToServer());

		document.getElementById('menu_configuration_close').addEventListener('click', () => Configuration.Close());
		document.getElementById('menu_configuration_push').addEventListener('click', () => Configuration.PushToServer());
		document.getElementById('menu_configuration_save').addEventListener('click', () => Backups.OpenSaveDialog());
		document.getElementById('menu_configuration_load').addEventListener('click', () => Backups.OpenLoadDialog());

		document.getElementById('menu_configuration_save_filesystem').addEventListener('click', () => Configuration.Save());
		document.getElementById('menu_configuration_save_as_filesystem').addEventListener('click', () => Configuration.SaveAs());
		document.getElementById('menu_configuration_load_filesystem').addEventListener('click', () => Configuration.OpenFromFileSystem());

		document.getElementById('menu_reports_matrices').addEventListener('click', () => Matrices.ExportMatrices());
		document.getElementById('menu_reports_data_validation_plan').addEventListener('click', () => Digests.DataValidationPlan());
		document.getElementById('menu_reports_field_models_relation').addEventListener('click', () => Digests.FieldModelRelations());

		document.getElementById('menu_advanced_show').addEventListener('click', () => ConfigurationSerialized.Show());
		document.getElementById('menu_advanced_download').addEventListener('click', () => Configuration.Download());
		document.getElementById('menu_advanced_logs').addEventListener('click', () => Logs.OpenDialog());

		document.getElementById('menu_wizards_payment').addEventListener('click', () => Wizards.Open('payment'));
		document.getElementById('menu_wizards_validator').addEventListener('click', () => Wizards.Open('validator'));
		document.getElementById('menu_wizards_form_model').addEventListener('click', () => Wizards.Open('form_model'));

		document.getElementById('menu_help_shortcuts').addEventListener('click', () => Shortcuts.Open());
		document.getElementById('menu_help_statistics').addEventListener('click', () => Statistics.Open());

		document.getElementById('menu_configuration_push_icon').addEventListener('click', () => Configuration.PushToServer());
		document.getElementById('menu_configuration_save_icon').addEventListener('click', () => Configuration.Save());

		add_top_item_listeners(menubar.querySelectorAll(':scope > li > :is(a, button)'));
		add_item_listeners(menubar.querySelectorAll(':scope > li > ul > li :is(a, button)'));

		bus_ui.register({
			onLoadStudy: function(event) {
				//languages menu
				function menu_language_listener() {
					Languages.ChangeLanguage(this.dataset.languageId);
					menu_languages.children.forEach(menu_language => {
						menu_language.firstElementChild.firstElementChild.src = menu_language === this.parentNode ? 'images/tick.png' : 'images/untick.png';
					});
				}

				function draw_menu_language(language) {
					const menu_language_li = document.createFullElement('li');
					const menu_language_button = document.createFullElement('button', {tabindex: '0', 'data-language-id': language.id});
					menu_language_button.addEventListener('click', menu_language_listener);
					add_item_listeners([menu_language_button]);
					menu_language_button.appendChild(document.createFullElement('img', {src: language.id === Languages.GetLanguage() ? 'images/tick.png' : 'images/untick.png'}));
					menu_language_button.appendChild(document.createTextNode(language.getLocalizedShortname(Languages.GetLanguage())));
					menu_language_li.appendChild(menu_language_button);
					return menu_language_li;
				}

				const menu_languages = document.getElementById('menu_languages');
				event.study.languages.map(draw_menu_language).forEach(Node.prototype.appendChild, menu_languages.empty());
			},
			onUpdateSetting: function(event) {
				const setting_menu = document.querySelector(`#menubar button[data-setting-id="${event.setting}"]`);
				if(setting_menu) {
					setting_menu.firstElementChild.src = event.value ? 'images/tick.png' : 'images/untick.png';
				}
			}
		});
	}
};
