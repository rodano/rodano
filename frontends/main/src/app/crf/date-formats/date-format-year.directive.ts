import {Directive} from '@angular/core';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {YEAR_FORMAT} from 'src/app/utils/app-date-formats';

@Directive({
	selector: '[appDateFormatYear]',
	providers: [{provide: MAT_DATE_FORMATS, useValue: YEAR_FORMAT}]
})
export class DateFormatYearDirective {
}
