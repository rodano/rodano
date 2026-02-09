import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {ProjectLanguage} from '@core/model/project-language';

@Injectable({
	providedIn: 'root'
})
export class LanguageService {
	private LanguageSubject = new BehaviorSubject<string>('');
	private projectLanguagesSubject = new BehaviorSubject<ProjectLanguage[]>([]);

	selectedLanguage$ = this.LanguageSubject.asObservable();
	projectLanguages$ = this.projectLanguagesSubject.asObservable();

	setLanguage(languageCode: string): void {
		this.LanguageSubject.next(languageCode);
	}

	get currentLanguage(): string {
		return this.LanguageSubject.value;
	}

	setProjectLanguages(languages: ProjectLanguage[]): void {
		this.projectLanguagesSubject.next(languages);
	}

	get projectLanguages(): ProjectLanguage[] {
		return this.projectLanguagesSubject.value;
	}

	getDefaultLanguageCode(languages?: ProjectLanguage[]): string {
		const langs = languages || this.projectLanguages;
		const defaultLang = langs.find(lang => lang.isDefault);
		return defaultLang?.languageCode || 'en';
	}

	getDefaultTranslation(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		const defaultLang = this.getDefaultLanguageCode();
		return translations[defaultLang] || '';
	}
}
