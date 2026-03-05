import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {ChartModel} from '@core/model/chart-model';
import {Workflow} from '@core/model/workflow';
import {WorkflowState} from '@core/model/workflow-state';
import {LanguageService} from '../../../services/language.service';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface ChartWorkflowStatusDialogData {
	chart: ChartModel;
	availableWorkflows: Workflow[];
	availableWorkflowStates: WorkflowState[];
}

@Component({
	selector: 'app-chart-workflow-status-dialog',
	standalone: true,
	templateUrl: './chart-workflow-status-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatIconModule, MatSelectModule]
})
export class ChartWorkflowStatusDialogComponent extends BaseDialogComponent<ChartWorkflowStatusDialogData> implements OnInit {
	form: FormGroup;

	includedSelected: WorkflowState[] = [];
	excludedSelected: WorkflowState[] = [];

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<ChartWorkflowStatusDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartWorkflowStatusDialogData
	) {
		super(dialogRef, data);
	}

	ngOnInit(): void {
		this.form = this.fb.group({
			workflowId: [this.data.chart?.workflowId ?? null]
		});

		this.form.get('workflowId')!.valueChanges.subscribe(() => {
			this.includedSelected = [];
			this.excludedSelected = [];
		});

		const stateFilters = this.data.chart?.stateFilters || [];
		stateFilters.forEach(sf => {
			const state = this.data.availableWorkflowStates.find(s => s.workflowStateId === sf.workflowStateId);
			if(!state) {
				return;
			}
			if(sf.kind === 'INCLUDED') {
				this.includedSelected.push(state);
			}
			if(sf.kind === 'EXCLUDED') {
				this.excludedSelected.push(state);
			}
		});
	}

	get filteredStates(): WorkflowState[] {
		const workflowId = this.form.value.workflowId;
		if(!workflowId) {
			return [];
		}
		return this.data.availableWorkflowStates.filter(s => s.workflowId === workflowId);
	}

	get includedAvailable(): WorkflowState[] {
		return this.filteredStates.filter(s =>
			!this.includedSelected.some(sel => sel.workflowStateId === s.workflowStateId)
		);
	}

	get excludedAvailable(): WorkflowState[] {
		return this.filteredStates.filter(s =>
			!this.excludedSelected.some(sel => sel.workflowStateId === s.workflowStateId)
		);
	}

	addIncluded(state: WorkflowState): void {
		if(!this.includedSelected.some(s => s.workflowStateId === state.workflowStateId)) {
			this.includedSelected = [...this.includedSelected, state];
		}
	}

	removeIncluded(state: WorkflowState): void {
		this.includedSelected = this.includedSelected.filter(s => s.workflowStateId !== state.workflowStateId);
	}

	addExcluded(state: WorkflowState): void {
		if(!this.excludedSelected.some(s => s.workflowStateId === state.workflowStateId)) {
			this.excludedSelected = [...this.excludedSelected, state];
		}
	}

	removeExcluded(state: WorkflowState): void {
		this.excludedSelected = this.excludedSelected.filter(s => s.workflowStateId !== state.workflowStateId);
	}

	getLabel(state: WorkflowState): string {
		return this.languageService.getTranslatedValue(state.shortname) || state.id;
	}

	onSave(): void {
		const stateFilters = [
			...this.includedSelected.map(s => ({workflowStateId: s.workflowStateId, kind: 'INCLUDED'})),
			...this.excludedSelected.map(s => ({workflowStateId: s.workflowStateId, kind: 'EXCLUDED'}))
		];

		this.dialogRef.close({
			workflowId: this.form.value.workflowId,
			stateFilters
		});
	}
}
