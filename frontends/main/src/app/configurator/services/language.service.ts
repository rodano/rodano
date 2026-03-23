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

	getLabel(entity: {id: string; shortname?: Record<string, string>; title?: Record<string, string>}): string {
		const name = this.getDefaultTranslation(entity.shortname) || this.getDefaultTranslation(entity.title) || entity.id;
		return `${name} (${entity.id})`;
	}

	getLabelById<T extends {id: string; shortname?: Record<string, string>}>(
		id: string,
		getById: (id: string) => T | undefined | null
	): string {
		const entity = getById(id);
		return entity ? this.getLabel(entity) : id;
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.currentLanguage;
		return translations[lang] || '';
	}
}
