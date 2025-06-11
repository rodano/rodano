import {Component, OnInit, Input, DestroyRef, OnChanges} from '@angular/core';
import {ReactiveFormsModule, FormControl} from '@angular/forms';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatFormField} from '@angular/material/form-field';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {merge} from 'rxjs';
import {FieldUpdateService} from '../../services/field-update.service';
import {CRFField} from '../../models/crf-field';

@Component({
	selector: 'app-date-select',
	templateUrl: './date-select.component.html',
	styleUrls: ['../field/field.component.css', './date-select.component.css'],
	imports: [
		ReactiveFormsModule,
		MatFormField,
		MatSelect,
		MatOption
	]
})
export class DateSelectComponent implements OnInit, OnChanges {
	@Input() field: CRFField;
	@Input() disabled: boolean;

	days: {label: string; value: string}[];
	months: {label: string; value: string}[];
	years: {label: string; value: string}[];

	day = new FormControl('');
	month = new FormControl('');
	year = new FormControl('');

	constructor(
		private fieldUpdateService: FieldUpdateService,
		private destroyRef: DestroyRef
	) { }

	ngOnInit(): void {
		const observables = [];
		//fill days
		if(this.field.model.withDays) {
			this.days = [];
			if(!this.field.model.daysMandatory) {
				this.days.push({label: 'Unknown', value: 'Unknown'});
			}
			for(let i = 1; i <= 31; i++) {
				const day = i.toString().padStart(2, '0');
				//pay attention to the value that is a string
				this.days.push({label: day, value: day});
			}
			observables.push(this.day.valueChanges);
		}

		//fill months
		if(this.field.model.withMonths) {
			this.months = [];
			if(!this.field.model.monthsMandatory) {
				this.months.push({label: 'Unknown', value: 'Unknown'});
			}
			for(let i = 1; i <= 12; i++) {
				const month = i.toString().padStart(2, '0');
				//pay attention to the value that is a string
				this.months.push({label: month, value: month});
			}
			observables.push(this.month.valueChanges);
		}

		//fill years
		this.years = [];
		if(!this.field.model.yearsMandatory) {
			this.years.push({label: 'Unknown', value: 'Unknown'});
		}
		const currentYear = new Date().getUTCFullYear();
		for(let i = this.field.model.maxYear || currentYear; i >= (this.field.model.minYear || 1900); i--) {
			const year = i.toString();
			//pay attention to the value that is a string
			this.years.push({label: year, value: year});
		}

		//set value
		const dateParts = this.field.value?.split('.') ?? ['', '', ''];
		this.year.reset(dateParts[dateParts.length - 1]);
		if(this.field.model.withMonths) {
			this.month.reset(dateParts[dateParts.length - 2]);
			if(this.field.model.withDays) {
				this.day.reset(dateParts[dateParts.length - 3]);
			}
		}
		observables.push(this.year.valueChanges);

		merge(...observables).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			//would be nicer to use the emitted values
			//but it makes it difficult to deal with initial values
			const parts = [];
			if(this.field.model.withDays) {
				parts.push(this.day.value);
			}
			if(this.field.model.withMonths) {
				parts.push(this.month.value);
			}
			parts.push(this.year.value);
			const fieldValue = parts.join('.');
			this.fieldUpdateService.updateField(this.field, fieldValue, fieldValue);
		});
	}

	ngOnChanges() {
		if(this.disabled) {
			this.day.disable();
			this.month.disable();
			this.year.disable();
		}
		else {
			this.day.enable();
			this.month.enable();
			this.year.enable();
		}
	}
}
