import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'dateUTC'
})
export class DateUTCPipe implements PipeTransform {
	transform(date: Date): string {
		if(!date) {
			return 'NA';
		}
		return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
	}
}
