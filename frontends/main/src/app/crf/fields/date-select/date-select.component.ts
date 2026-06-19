import {Component, OnInit, DestroyRef, computed, effect, input} from '@angular/core';
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
export class DateSelectComponent implements OnInit {
	readonly field = input.required<CRFField>();
	readonly disabled = input<boolean>(false);

	readonly days = computed<{label: string; value: string}[]>(() => {
		const field = this.field();
		if(!field.model.withDays) {
			return [];
		}
		const result: {label: string; value: string}[] = [];
		if(!field.model.daysMandatory) {
			result.push({label: 'Unknown', value: 'Unknown'});
		}
		for(let i = 1; i <= 31; i++) {
			const day = i.toString().padStart(2, '0');
			result.push({label: day, value: day});
		}
		return result;
	});

	readonly months = computed<{label: string; value: string}[]>(() => {
		const field = this.field();
		if(!field.model.withMonths) {
			return [];
		}
		const result: {label: string; value: string}[] = [];
		if(!field.model.monthsMandatory) {
			result.push({label: 'Unknown', value: 'Unknown'});
		}
		for(let i = 1; i <= 12; i++) {
			const month = i.toString().padStart(2, '0');
			result.push({label: month, value: month});
		}
		return result;
	});

	readonly years = computed<{label: string; value: string}[]>(() => {
		const field = this.field();
		const result: {label: string; value: string}[] = [];
		if(!field.model.yearsMandatory) {
			result.push({label: 'Unknown', value: 'Unknown'});
		}
		const currentYear = new Date().getUTCFullYear();
		for(let i = field.model.maxYear || currentYear; i >= (field.model.minYear || 1900); i--) {
			result.push({label: i.toString(), value: i.toString()});
		}
		return result;
	});

	day = new FormControl('');
	month = new FormControl('');
	year = new FormControl('');

	constructor(
		private fieldUpdateService: FieldUpdateService,
		private destroyRef: DestroyRef
	) {
		effect(() => {
			if(this.disabled()) {
				this.day.disable({emitEvent: false});
				this.month.disable({emitEvent: false});
				this.year.disable({emitEvent: false});
			}
			else {
				this.day.enable({emitEvent: false});
				this.month.enable({emitEvent: false});
				this.year.enable({emitEvent: false});
			}
		});

		effect(() => {
			const field = this.field();
			const dateParts = field.value?.split('.') ?? ['', '', ''];
			this.year.reset(dateParts[dateParts.length - 1], {emitEvent: false});
			if(field.model.withMonths) {
				this.month.reset(dateParts[dateParts.length - 2], {emitEvent: false});
				if(field.model.withDays) {
					this.day.reset(dateParts[dateParts.length - 3], {emitEvent: false});
				}
			}
		});
	}

	ngOnInit(): void {
		merge(this.day.valueChanges, this.month.valueChanges, this.year.valueChanges).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe(() => {
			const field = this.field();
			const parts = [];
			if(field.model.withDays) {
				parts.push(this.day.value);
			}
			if(field.model.withMonths) {
				parts.push(this.month.value);
			}
			parts.push(this.year.value);
			const fieldValue = parts.join('.');
			this.fieldUpdateService.updateField(field, fieldValue, fieldValue);
		});
	}
}
