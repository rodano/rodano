import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class LanguageService {
	private LanguageSubject = new BehaviorSubject<string>('');

	selectedLanguage$ = this.LanguageSubject.asObservable();

	setLanguage(languageCode: string): void {
		this.LanguageSubject.next(languageCode);
	}

	get currentLanguage(): string {
		return this.LanguageSubject.value;
	}
}
