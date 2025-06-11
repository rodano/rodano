import { Pipe, PipeTransform } from '@angular/core';
import { AppService } from '../services/app.service';

@Pipe({
	name: 'localizer',
	standalone: true
})
export class LocalizerPipe implements PipeTransform {
	constructor(
		private appService: AppService
	) { }

	transform(object: Record<string, string>, localizeLanguageId?: string): string {
		const languageId = localizeLanguageId || this.appService.getSelectedLanguageId();
		if(object) {
			return object[languageId];
		} else {
			return '';
		}
	}
}
