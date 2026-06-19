import {Service} from '@angular/core';

@Service()
export class SettingsService {
	private static STORAGE_KEY = 'settings';

	settings: Record<string, any> = {};

	constructor() {
		this.settings = JSON.parse(localStorage.getItem(SettingsService.STORAGE_KEY) || '{}');
	}

	saveSettings() {
		localStorage.setItem(SettingsService.STORAGE_KEY, JSON.stringify(this.settings));
	}

	set(key: string, value: any) {
		this.settings[key] = value;
		this.saveSettings();
	}

	get<T>(key: string, defaultValue: T): T {
		if(this.settings.hasOwnProperty(key)) {
			return this.settings[key];
		}
		return defaultValue;
	}
}
