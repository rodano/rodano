import {ScopeModel} from '@core/model/scope-model';
import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBar} from '@angular/material/snack-bar';
import {WorkflowState} from '@core/model/workflow-state';

interface DialogData {
	projectId: string;
	scopeModel: ScopeModel;
	selectedLanguage: string;
}

@Component({
	selector: 'app-scope-model-resources-dialog',
	standalone: true,
	templateUrl: './scope-model-resources-dialog.component.html',
	styleUrls: ['../shared-scope-model-dialog-styles.css'],
	imports: [
		CommonModule,
		ReactiveFormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatInputModule,
		MatButtonModule,
		MatSelectModule
	]
})
export class ScopeModelResourcesDialogComponent implements OnInit {
	form: FormGroup;
	saving = false;

	//TODO: Load these from backend
	availableDatasets: any[] = [];
	availableForms: any[] = [];
	availableWorkflows: any[] = [];
	availableWorkflowStates: any[] = [];

	constructor(
		private fb: FormBuilder,
		private dialogRef: MatDialogRef<ScopeModelResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: DialogData,
		private snackBar: MatSnackBar
	) {
		this.form = this.fb.group({
			datasetModelIds: [[]],
			formModelIds: [[]],
			workflowIds: [[]],
			workflowStateIds: [[]]
		});
	}

	ngOnInit(): void {
		this.populateForm();
		//TODO: Load available resources from backend
		//this.loadAvailableResources();

		this.form.get('workflowIds')?.valueChanges.subscribe(() => {
			this.updateFilteredStates();
		});

		this.updateFilteredStates();
	}

	populateForm(): void {
		this.form.patchValue({
			datasetModelIds: this.data.scopeModel.datasetModelIds || [],
			formModelIds: this.data.scopeModel.formModelIds || [],
			workflowIds: this.data.scopeModel.workflowIds || [],
			workflowStateIds: (this.data.scopeModel as any).workflowStateIds || []
		});
	}

	updateFilteredStates(): void {
		const rawValue = this.form.get('workflowIds')?.value;
		const selectedWorkflowIds: string[] = Array.isArray(rawValue) ? rawValue : [];

		if(selectedWorkflowIds.length === 0) {
			this.availableWorkflowStates = [];
			return;
		}

		const activeWorkflows = this.availableWorkflows.filter(w =>
			selectedWorkflowIds.includes(w.workflowId)
		);

		this.availableWorkflowStates = activeWorkflows.flatMap(w =>
			w.states.map((state: any) => ({
				id: state.id,
				name: state.shortname[this.data.selectedLanguage] || state.id,
				workflowName: w.shortname[this.data.selectedLanguage] || w.id
			}))
		);

		const currentSelectedStateIds = (this.form.get('workflowStateIds')?.value as string[]) || [];
		const validStateIds = this.availableWorkflowStates.map(s => s.id);
		const filteredSelection = currentSelectedStateIds.filter(id => validStateIds.includes(id));

		if(filteredSelection.length !== currentSelectedStateIds.length) {
			this.form.get('workflowStateIds')?.setValue(filteredSelection, {emitEvent: false});
		}
	}

	onSave(): void {
		if(this.form.invalid) {
			this.snackBar.open('Please fill in all required fields', 'Close', {duration: 3000});
			return;
		}

		const formValue = this.form.getRawValue();

		const result = {
			datasetModelIds: formValue.datasetModelIds || [],
			formModelIds: formValue.formModelIds || [],
			workflowIds: formValue.workflowIds || [],
			workflowStateIds: formValue.workflowStateIds || []
		};

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
