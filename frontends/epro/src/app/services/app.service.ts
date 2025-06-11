import { Injectable, EventEmitter } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable()
export class AppService {

	private static STORAGE_KEY_SELECTED_LANGUAGE_ID = 'selectedLanguageId';

	public connectedStatus: EventEmitter<boolean>;

	constructor() {
		this.connectedStatus = new EventEmitter();
	}

	isDevMode() {
		return !environment.production;
	}

	public updateConnectedStatus() {
		this.connectedStatus.emit(true);
	}

	public setSelectedLanguageId(languageId: string) {
		localStorage.setItem(AppService.STORAGE_KEY_SELECTED_LANGUAGE_ID, languageId);
	}

	public deleteSelectedLanguageId() {
		localStorage.removeItem(AppService.STORAGE_KEY_SELECTED_LANGUAGE_ID);
	}

	public getSelectedLanguageId(): string {
		const selectedLanguageId = localStorage.getItem(AppService.STORAGE_KEY_SELECTED_LANGUAGE_ID);
		if(selectedLanguageId === null) {
			throw new Error('User language not defined');
		}
		return selectedLanguageId;
	}

	public hasSelectedLanguageId(): boolean {
		return !!this.getSelectedLanguageId();
	}
}
