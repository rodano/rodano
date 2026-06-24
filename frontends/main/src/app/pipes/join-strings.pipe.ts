import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'joinStrings'
})
export class JoinStringsPipe implements PipeTransform {
	transform(value: string[], maxLength = 1): string {
		if(value.length > maxLength) {
			return value.slice(0, maxLength).join(', ').concat(', [...]');
		}
		return value.join(', ');
	}
}
