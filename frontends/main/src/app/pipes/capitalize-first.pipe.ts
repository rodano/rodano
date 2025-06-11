import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
	name: 'capitalizeFirst'
})
export class CapitalizeFirstPipe implements PipeTransform {
	transform(value: string | undefined): string {
		if(value) {
			const newValue = value.split('_').join(' ');
			return newValue[0].toUpperCase() + newValue.substring(1).toLowerCase();
		}
		else {
			return '';
		}
	}
}
