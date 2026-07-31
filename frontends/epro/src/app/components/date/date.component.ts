import {Component, DestroyRef, OnInit, effect, inject, input, model} from '@angular/core';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatDatepicker, MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormField} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {format, parse} from 'date-fns';
import {merge} from 'rxjs';
import {Field} from '@core/model/field';
import {FieldService} from '@core/services/field.service';
import {DateFormatMonthYearDirective} from '../../directives/date-format-month-year.directive';
import {DateFormatYearDirective} from '../../directives/date-format-year.directive';

@Component({
	selector: 'app-date',
	templateUrl: './date.component.html',
	styleUrls: ['./date.component.css'],
	imports: [
		MatFormField,
		MatInputModule,
		MatDatepickerModule,
		ReactiveFormsModule,
		DateFormatMonthYearDirective,
		DateFormatYearDirective
	]
})
export class DateComponent implements OnInit {
	private destroyRef = inject(DestroyRef);
	private fieldService = inject(FieldService);

	readonly field = input.required<Field>();
	readonly disabled = input<boolean>(false);
	readonly value = model<string | undefined>();

	readonly dateControl = new FormControl<Date | null>(null);
	readonly timeControl = new FormControl<string | null>('');

	constructor() {
		effect(() => {
			if(this.disabled()) {
				this.dateControl.disable({emitEvent: false});
				this.timeControl.disable({emitEvent: false});
			}
			else {
				this.dateControl.enable({emitEvent: false});
				this.timeControl.enable({emitEvent: false});
			}
		});

		effect(() => {
			const field = this.field();
			const value = this.value();
			this.timeControl.setValidators([this.getValidator()]);
			if(value) {
				const parts = value.split(' ');
				if(this.isDate()) {
					const datePart = parts.shift() as string;
					const dateFormat = this.fieldService.generateDateFormat(field.model);
					this.dateControl.reset(parse(datePart, dateFormat, new Date()), {emitEvent: false});
				}
				if(this.isTime()) {
					this.timeControl.reset(parts.shift(), {emitEvent: false});
				}
			}
			else {
				this.dateControl.reset(null, {emitEvent: false});
				this.timeControl.reset(null, {emitEvent: false});
			}
		});
	}

	ngOnInit() {
		merge(this.dateControl.valueChanges, this.timeControl.valueChanges).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			if(this.dateControl.valid && this.timeControl.valid) {
				const parts = [];
				if(this.isDate()) {
					const dateFormat = this.fieldService.generateDateFormat(this.field().model);
					parts.push(this.dateControl.value ? format(this.dateControl.value, dateFormat) : '');
				}
				if(this.isTime()) {
					parts.push(this.timeControl.value ?? '');
				}
				this.value.set(parts.join(' '));
			}
		});
	}

	getValidator() {
		const model = this.field().model;
		let regexp = '[01]\\d|2[0-3]';
		if(model.withMinutes) {
			regexp += ':[0-5]\\d';
		}
		if(model.withSeconds) {
			regexp += ':[0-5]\\d';
		}
		return Validators.pattern(new RegExp(`^${regexp}$`));
	}

	getPlaceholder() {
		const model = this.field().model;
		let placeholder = 'HH';
		if(model.withMinutes) {
			placeholder += ':MM';
		}
		if(model.withSeconds) {
			placeholder += ':SS';
		}
		return placeholder;
	}

	isDate() {
		return this.fieldService.isDate(this.field().model);
	}

	isTime() {
		return this.fieldService.isTime(this.field().model);
	}

	yearHandler(newValue: Date, picker: MatDatepicker<Date>) {
		this.dateControl.setValue(newValue);
		if(!this.field().model.withMonths) {
			picker.close();
		}
	}

	monthHandler(newValue: Date, picker: MatDatepicker<Date>) {
		this.dateControl.setValue(newValue);
		if(!this.field().model.withDays) {
			picker.close();
		}
	}
}
