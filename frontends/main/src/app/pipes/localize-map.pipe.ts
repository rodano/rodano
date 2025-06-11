import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'localizeMap'
})
export class LocalizeMapPipe implements PipeTransform {
	transform(label: Record<string, string> | undefined): string {
		return label?.en || '';
	}
}
