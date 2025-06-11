import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'dateTimeUTC'
})
export class DateTimeUTCPipe implements PipeTransform {
	transform(date: Date | undefined): string {
		if(!date) {
			return 'NA';
		}
		let label = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
		label += ' ';
		label += `${date.getUTCHours().toString().padStart(2, '0')}:${date.getUTCMinutes().toString().padStart(2, '0')}:${date.getUTCSeconds().toString().padStart(2, '0')}`;
		return label;
	}
}
