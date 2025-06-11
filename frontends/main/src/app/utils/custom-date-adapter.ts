import {NativeDateAdapter} from '@angular/material/core';
import {format} from 'date-fns';
import {Injectable} from '@angular/core';

@Injectable({
	providedIn: 'root'
})
export class CustomDateAdapter extends NativeDateAdapter {
	override createDate(year: number, month: number, date: number): Date {
		//create a UTC date from the parts provided by the date component
		const utcDate = new Date(0);
		utcDate.setUTCFullYear(year);
		utcDate.setUTCMonth(month);
		utcDate.setUTCDate(date);
		return utcDate;
	}

	override format(date: Date, displayFormat: string): string {
		switch(displayFormat) {
			case 'year':
				return format(date, 'yyyy');
			case 'month-year':
				return format(date, 'yyyy-MM');
			default:
				return format(date, 'yyyy-MM-dd');
		}
	}
}
