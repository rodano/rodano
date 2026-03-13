import {ChangeDetectionStrategy, Component, OnInit, DestroyRef, effect, input} from '@angular/core';
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
	changeDetection: ChangeDetectionStrategy.OnPush,
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
export class DateComponent implements OnInit {
	readonly field = input.required<CRFField>();
	readonly disabled = input<boolean>(false);

	dateControl = new FormControl<Date>(new Date());
	timeControl = new FormControl<string>('');

	constructor(
		private destroyRef: DestroyRef,
		private fieldService: FieldService,
		private fieldUpdateService: FieldUpdateService
	) {
		effect(() => {
			if(this.field().model.readOnly || this.disabled()) {
				this.dateControl.disable({emitEvent: false});
			}
			else {
				this.dateControl.enable({emitEvent: false});
			}
		});

		effect(() => {
			const field = this.field();
			this.timeControl.setValidators([this.getValidator()]);
			if(field.value) {
				const parts = field.value.split(' ');
				if(this.isDate()) {
					const datePart = parts.shift();
					const dateFormat = this.fieldService.generateDateFormat(field.model);
					const dateValue = parse(datePart as string, dateFormat, new Date());
					this.dateControl.reset(dateValue, {emitEvent: false});
				}
				if(this.isTime()) {
					const timePart = parts.shift();
					this.timeControl.reset(timePart, {emitEvent: false});
				}
			}
			else {
				this.dateControl.reset(undefined, {emitEvent: false});
				this.timeControl.reset(undefined, {emitEvent: false});
			}
		});
	}

	ngOnInit(): void {
		merge(this.dateControl.valueChanges, this.timeControl.valueChanges).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(value => {
			if(this.dateControl.valid && this.timeControl.valid) {
				const field = this.field();
				const parts = [];
				if(this.isDate()) {
					const fieldFormat = this.fieldService.generateDateFormat(field.model);
					parts.push(value ? format(this.dateControl.value as Date, fieldFormat) : '');
				}
				if(this.isTime()) {
					parts.push(this.timeControl.value);
				}
				const fieldValue = parts.join(' ');
				this.fieldUpdateService.updateField(field, fieldValue, fieldValue);
			}
		});
	}

	getValidator() {
		const field = this.field();
		let regexp = '[01]\\d|2[0-3]';
		if(field.model.withMinutes) {
			regexp += ':[0-5]\\d';
		}
		if(field.model.withSeconds) {
			regexp += ':[0-5]\\d';
		}
		return Validators.pattern(new RegExp(`^${regexp}$`));
	}

	getPlaceholder() {
		const field = this.field();
		let placeholder = 'HH';
		if(field.model.withMinutes) {
			placeholder += ':MM';
		}
		if(field.model.withSeconds) {
			placeholder += ':SS';
		}
		return placeholder;
	}

	getTimeSize() {
		const field = this.field();
		let size = 2;
		if(field.model.withMinutes) {
			size += 3;
		}
		if(field.model.withSeconds) {
			size += 3;
		}
		return size;
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
