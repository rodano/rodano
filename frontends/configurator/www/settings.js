import {BusEventUpdateSetting, bus_ui} from './bus_ui.js';

const LOCAL_STORAGE_KEY = 'rodano.configurator.settings';

//cache current settings
let settings = undefined;

function read_settings() {
	if(!settings) {
		//retrieve settings from local settings
		try {
			settings = localStorage.getObject(LOCAL_STORAGE_KEY) || {};
		}
		//TODO delete this as settings should not have been corrupted
		catch {
			settings = {};
		}
	}
	return settings;
}

function write_settings(settings) {
	localStorage.setObject(LOCAL_STORAGE_KEY, settings);
}

export const Settings = {
	Exists(key) {
		const settings = read_settings();
		return settings.hasOwnProperty(key);
	},
	Set: function(key, value) {
		const settings = read_settings();
		settings[key] = value;
		bus_ui.dispatch(new BusEventUpdateSetting(key, value));
		write_settings(settings);
	},
	Get: function(key, default_value) {
		const settings = read_settings();
		return settings.hasOwnProperty(key) ? settings[key] : default_value;
	},
	All: function() {
		return Object.assign({}, read_settings());
	},
	Remove: function(key) {
		const settings = read_settings();
		delete settings[key];
		write_settings(settings);
	}
};
