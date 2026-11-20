import {Pipe, PipeTransform} from '@angular/core';
import {EproLanguageService} from '../services/epro-language.service';

@Pipe({
	name: 'localizer',
	standalone: true
})
export class LocalizerPipe implements PipeTransform {
	constructor(
		private languageService: EproLanguageService
	) { }

	transform(object: Record<string, string>, localizeLanguageId?: string): string {
		const languageId = localizeLanguageId || this.languageService.getSelectedLanguageId();
		if(object) {
			return object[languageId];
		}
		else {
			return '';
		}
	}
}
