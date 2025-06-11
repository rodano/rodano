import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'shortenString'
})
export class ShortenStringPipe implements PipeTransform {
	transform(value: string, length?: number): string {
		length = length ? length : 1;
		if(value.length > 1) {
			return value.slice(0, length).toString().concat(', [...]').toString();
		}
		return value;
	}
}
