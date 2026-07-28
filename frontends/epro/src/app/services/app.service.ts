import {Injectable, isDevMode} from '@angular/core';

@Injectable()
export class AppService {
	private static readonly LANGUAGE_KEY = 'selectedLanguageId';

	getSelectedLanguageId(): string {
		return localStorage.getItem(AppService.LANGUAGE_KEY) ?? 'en';
	}

	setSelectedLanguageId(id: string): void {
		localStorage.setItem(AppService.LANGUAGE_KEY, id);
	}

	isDevMode(): boolean {
		return isDevMode();
	}

	updateConnectedStatus(): void {
		//Triggers a connectivity state update; navigation is handled by the caller
	}
}
