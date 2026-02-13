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
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {FormulaAutocompleteService, FormulaProposal} from '../../../services/api/formula-autocomplete.service';
import {Observable, of} from 'rxjs';
import {map, startWith} from 'rxjs/operators';

export interface FieldModelCalculatedValueDialogData {
	fieldModel: FieldModel;
	projectId: string;
}

@Component({
	selector: 'app-field-model-calculated-value-dialog',
	standalone: true,
	templateUrl: './field-model-calculated-value-dialog.component.html',
	styleUrls: ['./field-model-calculated-value-dialog.component.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatIconModule,
		MatCheckboxModule,
		MatAutocompleteModule
	]
})
export class FieldModelCalculatedValueDialogComponent implements OnInit {
	form: FormGroup;
	filteredProposals$: Observable<FormulaProposal[]> = of([]);
	allProposals: FormulaProposal[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<FieldModelCalculatedValueDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: FieldModelCalculatedValueDialogData,
		private snackBar: MatSnackBar,
		private formulaAutocompleteService: FormulaAutocompleteService
	) {}

	ngOnInit(): void {
		this.initializeForm();
		this.loadAutocompleteProposals();
	}

	initializeForm(): void {
		const fm = this.data.fieldModel;

		this.form = this.fb.group({
			plugin: [fm.plugin || false],
			valueFormula: [fm.valueFormula || '']
		});
	}

	loadAutocompleteProposals(): void {
		this.formulaAutocompleteService.getAllProposals(this.data.projectId).subscribe({
			next: proposals => {
				this.allProposals = proposals;
				this.setupAutocomplete();
			},
			error: error => {
				console.error('Failed to load formula proposals:', error);
				this.snackBar.open('Failed to load autocomplete suggestions', 'Close', {duration: 3000});
			}
		});
	}

	setupAutocomplete(): void {
		this.filteredProposals$ = this.form.get('valueFormula')!.valueChanges.pipe(
			startWith(''),
			map(value => this.filterProposals(value || ''))
		);
	}

	private filterProposals(value: string | FormulaProposal): FormulaProposal[] {
		if(typeof value !== 'string') {
			return [];
		}

		if(!value || !value.startsWith('=')) {
			return [];
		}

		const searchTerm = value.substring(1).toUpperCase();

		return this.allProposals
			.filter(proposal =>
				proposal.id.toUpperCase().includes(searchTerm)
				|| proposal.label.toUpperCase().includes(searchTerm)
			)
			.slice(0, 10);
	}

	onProposalSelected(event: any): void {
		const proposal = event.option.value as FormulaProposal;
		this.form.get('valueFormula')!.setValue(proposal.value, {emitEvent: false});
	}

	displayProposal(proposal: FormulaProposal): string {
		return proposal ? proposal.value : '';
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}

	onSave(): void {
		const formValue = this.form.getRawValue();

		const result = {
			plugin: formValue.plugin,
			valueFormula: formValue.valueFormula
		};

		this.dialogRef.close(result);
	}
}
