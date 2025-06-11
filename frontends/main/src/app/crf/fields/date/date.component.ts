import {Component, OnInit, Input, DestroyRef, OnChanges} from '@angular/core';
import {ReactiveFormsModule, FormControl, Validators} from '@angular/forms';
import {MatDatepicker, MatDatepickerModule} from '@angular/material/datepicker';
import {parse, format} from 'date-fns';
import {DateFormatYearDirective} from '../../date-formats/date-format-year.directive';
import {DateFormatMonthYearDirective} from '../../date-formats/date-format-month-year.directive';
import {MatInputModule} from '@angular/material/input';
import {MatFormField} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {CRFField} from '../../models/crf-field';
import {FieldUpdateService} from '../../services/field-update.service';
import {FieldService} from '@core/services/field.service';
import {merge} from 'rxjs';

@Component({
	selector: 'app-date',
	templateUrl: './date.component.html',
	styleUrls: ['../field/field.component.css', './date.component.css'],
	imports: [
		MatFormField,
		MatInputModule,
		MatDatepickerModule,
		ReactiveFormsModule,
		DateFormatMonthYearDirective,
		DateFormatYearDirective
	]
})
export class DateComponent implements OnInit, OnChanges {
	@Input() field: CRFField;
	@Input() disabled: boolean;

	dateControl = new FormControl<Date>(new Date());
	timeControl = new FormControl<string>('');

	constructor(
		private destroyRef: DestroyRef,
		private fieldService: FieldService,
		private fieldUpdateService: FieldUpdateService
	) { }

	ngOnInit(): void {
		const observables = [];
		if(this.isDate()) {
			observables.push(this.dateControl.valueChanges);
		}
		if(this.isTime()) {
			observables.push(this.timeControl.valueChanges);
		}
		merge(...observables).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			if(this.dateControl.valid && this.timeControl.valid) {
				const parts = [];
				if(this.isDate()) {
					const fieldFormat = this.fieldService.generateDateFormat(this.field.model);
					parts.push(value ? format(this.dateControl.value as Date, fieldFormat) : '');
				}
				if(this.isTime()) {
					parts.push(this.timeControl.value);
				}
				const fieldValue = parts.join(' ');
				this.fieldUpdateService.updateField(this.field, fieldValue, fieldValue);
			}
		});
	}

	ngOnChanges() {
		//adjust time validator
		this.timeControl.setValidators([this.getValidator()]);
		if(this.field.value) {
			const parts = this.field.value.split(' ');
			if(this.isDate()) {
				const datePart = parts.shift();
				const dateFormat = this.fieldService.generateDateFormat(this.field.model);
				const dateValue = parse(datePart as string, dateFormat, new Date());
				this.dateControl.reset(dateValue);
			}
			if(this.isTime()) {
				const timePart = parts.shift();
				this.timeControl.reset(timePart);
			}
		}
		else {
			this.dateControl.reset(undefined);
			this.timeControl.reset(undefined);
		}

		if(this.field.model.readOnly || this.disabled) {
			this.dateControl.disable();
		}
	}

	getValidator() {
		let regexp = '[01]\\d|2[0-3]';
		if(this.field.model.withMinutes) {
			regexp += ':[0-5]\\d';
		}
		if(this.field.model.withSeconds) {
			regexp += ':[0-5]\\d';
		}
		return Validators.pattern(new RegExp(`^${regexp}$`));
	}

	getPlaceholder() {
		let placeholder = 'HH';
		if(this.field.model.withMinutes) {
			placeholder += ':MM';
		}
		if(this.field.model.withSeconds) {
			placeholder += ':SS';
		}
		return placeholder;
	}

	getTimeSize() {
		let size = 2;
		if(this.field.model.withMinutes) {
			size += 3;
		}
		if(this.field.model.withSeconds) {
			size += 3;
		}
		return size;
	}

	isDate() {
		return this.fieldService.isDate(this.field.model);
	}

	isTime() {
		return this.fieldService.isTime(this.field.model);
	}

	yearHandler(newValue: Date, picker: MatDatepicker<Date>) {
		this.dateControl.setValue(newValue);
		if(!this.field.model.withMonths) {
			picker.close();
		}
	}

	monthHandler(newValue: Date, picker: MatDatepicker<Date>) {
		this.dateControl.setValue(newValue);
		if(!this.field.model.withDays) {
			picker.close();
		}
	}
}
