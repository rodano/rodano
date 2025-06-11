import {Settings} from './settings.js';

const DEFAULT_THEME = 'Dark';
const THEMES = [DEFAULT_THEME, 'Light'];

const SETTING_SELECTED_STYLESHEET = 'selected_stylesheet';

//This module manages themes manually because there is no reliable standard way to do it

//Chrome does not support alternate stylesheets natively. Neither to select stylesheets with Javascript.
//http://stackoverflow.com/questions/29855651/alternate-stylesheets-not-working-in-chrome
//https://bugs.chromium.org/p/chromium/issues/detail?id=62434

export const Themes = {
	Init: function() {
		let theme = Themes.GetSelectedTheme();
		//check if theme is still valid
		if(!THEMES.includes(theme)) {
			theme = DEFAULT_THEME;
		}
		Themes.SelectTheme(theme);
	},
	GetThemes: function() {
		return THEMES.slice();
	},
	GetSelectedTheme: function() {
		return Settings.Get(SETTING_SELECTED_STYLESHEET, DEFAULT_THEME);
	},
	SelectTheme: function(theme) {
		//check if theme is still valid
		let link = document.querySelector('head > link[type="text/css"][data-type="theme"]');
		if(!link) {
			link = document.createFullElement('link', {type: 'text/css', rel: 'stylesheet'});
			document.head.appendChild(link);
		}
		link.setAttribute('href', `css/theme-${theme.toLowerCase()}.css`);
		Settings.Set(SETTING_SELECTED_STYLESHEET, theme);
	}
};
