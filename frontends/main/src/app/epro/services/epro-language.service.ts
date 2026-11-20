import {Service} from '@angular/core';

@Service()
export class EproLanguageService {
	private static readonly LANGUAGE_KEY = 'eproSelectedLanguageId';

	getSelectedLanguageId(): string {
		return localStorage.getItem(EproLanguageService.LANGUAGE_KEY) ?? 'en';
	}

	setSelectedLanguageId(id: string): void {
		localStorage.setItem(EproLanguageService.LANGUAGE_KEY, id);
	}
}
