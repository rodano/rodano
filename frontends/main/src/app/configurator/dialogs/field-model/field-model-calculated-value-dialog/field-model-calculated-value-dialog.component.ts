import {FieldModel} from '@core/model/field-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {Observable, of} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface FieldModelCalculatedValueDialogData {
	fieldModel: FieldModel;
	projectId: string;
}

interface FormulaProposal {
	id: string;
	label: string;
	value: string;
	category: string;
}

@Component({
	selector: 'app-field-model-calculated-value-dialog',
	standalone: true,
	templateUrl: './field-model-calculated-value-dialog.component.html',
	styleUrls: ['./field-model-calculated-value-dialog.component.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule,
		MatIconModule, MatCheckboxModule, MatAutocompleteModule]
})
export class FieldModelCalculatedValueDialogComponent extends BaseDialogComponent<FieldModelCalculatedValueDialogData> implements OnInit {
	form: FormGroup;
	filteredProposals$: Observable<FormulaProposal[]> = of([]);

	private conditions: FormulaProposal[] = [
		{id: 'DATASET', label: 'Condition DATASET', value: 'DATASET', category: 'Condition'},
		{id: 'EVENT', label: 'Condition EVENT', value: 'EVENT', category: 'Condition'},
		{id: 'FIELD', label: 'Condition FIELD', value: 'FIELD', category: 'Condition'},
		{id: 'SCOPE', label: 'Condition SCOPE', value: 'SCOPE', category: 'Condition'}
	];

	private functions: FormulaProposal[] = [
		//Date functions
		{id: 'CREATE_DATE', label: 'Create date from 6 components', value: '=CREATE_DATE(YEAR, MONTH, DAY, HOURS, MINUTES, SECONDS)', category: 'Function'},
		{id: 'TODAY', label: 'Today', value: '=TODAY()', category: 'Function'},
		{id: 'ADD_DURATION', label: 'Add duration to date', value: '=ADD_DURATION(DATE, YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS)', category: 'Function'},
		{id: 'ADD_YEARS', label: 'Add years to date', value: '=ADD_YEARS(DATE, YEARS)', category: 'Function'},
		{id: 'ADD_MONTHS', label: 'Add months to date', value: '=ADD_MONTHS(DATE, MONTHS)', category: 'Function'},
		{id: 'ADD_DAYS', label: 'Add days to date', value: '=ADD_DAYS(DATE, DAYS)', category: 'Function'},
		{id: 'ADD_HOURS', label: 'Add hours to date', value: '=ADD_HOURS(DATE, HOURS)', category: 'Function'},
		{id: 'ADD_MINUTES', label: 'Add minutes to date', value: '=ADD_MINUTES(DATE, MINUTES)', category: 'Function'},
		{id: 'ADD_SECONDS', label: 'Add seconds to date', value: '=ADD_SECONDS(DATE, SECONDS)', category: 'Function'},
		{id: 'DIFFERENCE_IN_YEARS', label: 'Difference in years', value: '=DIFFERENCE_IN_YEARS(DATE1, DATE2)', category: 'Function'},
		{id: 'DIFFERENCE_IN_MONTHS', label: 'Difference in months', value: '=DIFFERENCE_IN_MONTHS(DATE1, DATE2)', category: 'Function'},
		{id: 'DIFFERENCE_IN_DAYS', label: 'Difference in days', value: '=DIFFERENCE_IN_DAYS(DATE1, DATE2)', category: 'Function'},
		{id: 'DIFFERENCE_IN_SECONDS', label: 'Difference in seconds', value: '=DIFFERENCE_IN_SECONDS(DATE1, DATE2)', category: 'Function'},
		{id: 'MONTH_OF_DATE', label: 'Month of date', value: '=MONTH_OF_DATE(DATE)', category: 'Function'},
		//Condition functions
		{id: 'IF', label: 'If', value: '=IF(CONDITION, VALUE_IF_TRUE, VALUE_IF_FALSE)', category: 'Function'},
		{id: 'IS_EQUAL_TO', label: 'Is equal to', value: '=IS_EQUAL_TO(VALUE1, VALUE2)', category: 'Function'},
		//Conversion functions
		{id: 'NUMBER_TO_STRING', label: 'Number to string', value: '=NUMBER_TO_STRING(NUMBER)', category: 'Function'},
		{id: 'STRING_TO_NUMBER', label: 'String to number', value: '=STRING_TO_NUMBER(STRING)', category: 'Function'},
		//Math functions
		{id: 'SUM', label: 'Sum', value: '=SUM(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'MULTIPLY', label: 'Multiply', value: '=MULTIPLY(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'SUBTRACT', label: 'Subtract', value: '=SUBTRACT(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'DIVIDE', label: 'Divide', value: '=DIVIDE(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'POWER', label: 'Power', value: '=POWER(BASE, EXPONENT)', category: 'Function'},
		{id: 'SQRT', label: 'Square root', value: '=SQRT(NUMBER)', category: 'Function'},
		{id: 'INVERSE', label: 'Inverse', value: '=INVERSE(NUMBER)', category: 'Function'},
		{id: 'AVERAGE', label: 'Average', value: '=AVERAGE(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'MEDIAN', label: 'Median', value: '=MEDIAN(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'MIN', label: 'Minimum', value: '=MIN(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'MAX', label: 'Maximum', value: '=MAX(VALUE1, VALUE2, ...)', category: 'Function'},
		{id: 'MODULO', label: 'Modulo', value: '=MODULO(DIVIDEND, DIVISOR)', category: 'Function'},
		{id: 'ABS', label: 'Absolute value', value: '=ABS(NUMBER)', category: 'Function'},
		{id: 'ROUND', label: 'Round', value: '=ROUND(NUMBER, PRECISION)', category: 'Function'},
		{id: 'CEIL', label: 'Ceiling', value: '=CEIL(NUMBER)', category: 'Function'},
		{id: 'FLOOR', label: 'Floor', value: '=FLOOR(NUMBER)', category: 'Function'},
		{id: 'BMI', label: 'BMI', value: '=BMI(WEIGHT, HEIGHT_CM)', category: 'Function'}
	];

	private conditionProperties: Record<string, FormulaProposal[]> = {
		DATASET: [
			{id: 'SCOPE', label: 'Scope', value: '=DATASET:SCOPE', category: 'Properties for condition DATASET'},
			{id: 'EVENT', label: 'Event', value: '=DATASET:EVENT', category: 'Properties for condition DATASET'},
			{id: 'PK', label: 'Pk', value: '=DATASET:PK', category: 'Properties for condition DATASET'},
			{id: 'ID', label: 'Id', value: '=DATASET:ID', category: 'Properties for condition DATASET'},
			{id: 'REMOVED', label: 'Removed', value: '=DATASET:REMOVED', category: 'Properties for condition DATASET'},
			{id: 'IS_ATTACHED_TO_SCOPE', label: 'Is directly attached to scope', value: '=DATASET:IS_ATTACHED_TO_SCOPE', category: 'Properties for condition DATASET'},
			{id: 'FIELD', label: 'Fields', value: '=DATASET:FIELD', category: 'Properties for condition DATASET'},
			{id: 'CREATION_DATE', label: 'Creation date', value: '=DATASET:CREATION_DATE', category: 'Properties for condition DATASET'}
		],
		EVENT: [
			{id: 'SCOPE', label: 'Scope', value: '=EVENT:SCOPE', category: 'Properties for condition EVENT'},
			{id: 'PK', label: 'Pk', value: '=EVENT:PK', category: 'Properties for condition EVENT'},
			{id: 'ID', label: 'Id', value: '=EVENT:ID', category: 'Properties for condition EVENT'},
			{id: 'REMOVED', label: 'Removed', value: '=EVENT:REMOVED', category: 'Properties for condition EVENT'},
			{id: 'DATE', label: 'Date', value: '=EVENT:DATE', category: 'Properties for condition EVENT'}
		],
		FIELD: [
			{id: 'VALUE', label: 'Value', value: '=FIELD:VALUE', category: 'Properties for condition FIELD'},
			{id: 'ID', label: 'Id', value: '=FIELD:ID', category: 'Properties for condition FIELD'},
			{id: 'PK', label: 'Pk', value: '=FIELD:PK', category: 'Properties for condition FIELD'}
		],
		SCOPE: [
			{id: 'PK', label: 'Pk', value: '=SCOPE:PK', category: 'Properties for condition SCOPE'},
			{id: 'ID', label: 'Id', value: '=SCOPE:ID', category: 'Properties for condition SCOPE'},
			{id: 'REMOVED', label: 'Removed', value: '=SCOPE:REMOVED', category: 'Properties for condition SCOPE'}
		]
	};

	constructor(
		private fb: FormBuilder,
		dialogRef: MatDialogRef<FieldModelCalculatedValueDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: FieldModelCalculatedValueDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.form = this.fb.group({
			plugin: [this.data.fieldModel.plugin || false],
			valueFormula: [this.data.fieldModel.valueFormula || '']
		});

		this.filteredProposals$ = this.form.get('valueFormula')!.valueChanges.pipe(
			startWith(this.data.fieldModel.valueFormula || ''),
			map(value => this.getProposals(typeof value === 'string' ? value : ''))
		);
	}

	private getProposals(value: string): FormulaProposal[] {
		if(!value.startsWith('=')) {
			return [];
		}

		const conditionMatch = value.match(/^=([A-Z_]+):(.*)$/i);
		if(conditionMatch) {
			const conditionId = conditionMatch[1].toUpperCase();
			const searchTerm = conditionMatch[2].toUpperCase();
			const props = this.conditionProperties[conditionId] || [];
			return props.filter(p =>
				!searchTerm
				|| p.id.toUpperCase().includes(searchTerm)
				|| p.label.toUpperCase().includes(searchTerm)
			).slice(0, 10);
		}

		const functionMatch = value.match(/^=([A-Z_]+)\((.*)$/i);
		if(functionMatch) {
			const functionId = functionMatch[1].toUpperCase();
			const fn = this.functions.find(f => f.id === functionId);
			return fn ? [fn] : [];
		}

		const searchTerm = value.substring(1).toUpperCase();
		const all = [...this.conditions, ...this.functions];
		return all.filter(p =>
			!searchTerm
			|| p.id.toUpperCase().includes(searchTerm)
			|| p.label.toUpperCase().includes(searchTerm)
		).slice(0, 10);
	}

	onProposalSelected(event: any): void {
		const proposal = event.option.value as FormulaProposal;
		if(proposal.category === 'Condition') {
			this.form.get('valueFormula')!.setValue(`=${proposal.id}:`, {emitEvent: true});
		}
		else {
			this.form.get('valueFormula')!.setValue(proposal.value, {emitEvent: false});
		}
	}

	highlightMatch(text: string, inputValue: string): string {
		if(!inputValue || !inputValue.startsWith('=')) {
			return text;
		}

		const conditionMatch = inputValue.match(/^=([A-Z_]+):(.*)$/i);
		const searchTerm = conditionMatch
			? conditionMatch[2]
			: inputValue.substring(1);

		if(!searchTerm) {
			return text;
		}

		const regex = new RegExp(`(${searchTerm})`, 'gi');
		return text.replace(regex, '<mark>$1</mark>');
	}

	getCategoryClass(category: string): string {
		return `badge-${category.toLowerCase().replace(/\s+/g, '-')}`;
	}

	onSave(): void {
		this.dialogRef.close(this.form.getRawValue());
	}
}
