import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'abbreviate',
	standalone: true
})
export class AbbreviatePipe implements PipeTransform {
	transform(value: string | undefined, maxLength = 40): string {
		if(!value) {
			return '';
		}
		if(value.length <= maxLength) {
			return value;
		}
		return `${value.slice(0, maxLength)}...`;
	}
}
