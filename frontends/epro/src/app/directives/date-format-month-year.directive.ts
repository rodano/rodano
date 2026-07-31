import {Directive} from '@angular/core';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {MONTH_YEAR_FORMAT} from '../utils/app-date-formats';

@Directive({
	selector: '[appDateFormatMonthYear]',
	providers: [{provide: MAT_DATE_FORMATS, useValue: MONTH_YEAR_FORMAT}]
})
export class DateFormatMonthYearDirective {
}
