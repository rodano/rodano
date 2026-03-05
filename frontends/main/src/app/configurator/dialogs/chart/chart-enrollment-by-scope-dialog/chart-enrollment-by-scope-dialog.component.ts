import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ChartModel} from '@core/model/chart-model';
import {ScopeModel} from '@core/model/scope-model';
import {LanguageService} from '../../../services/language.service';
import {MatSelectModule} from '@angular/material/select';
import {BaseDialogComponent} from '../../base-dialog.component';

export interface ChartEnrollmentByScopeDialogData {
	chart: ChartModel;
	availableScopeModels: ScopeModel[];
}

@Component({
	selector: 'app-chart-enrollment-by-scope-dialog',
	standalone: true,
	templateUrl: './chart-enrollment-by-scope-dialog.component.html',
	styleUrls: ['../../dialog-shared.css'],
	imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatSelectModule]
})
export class ChartEnrollmentByScopeDialogComponent extends BaseDialogComponent<ChartEnrollmentByScopeDialogData> implements OnInit {
	form: FormGroup;

	constructor(
		private fb: FormBuilder,
		public languageService: LanguageService,
		dialogRef: MatDialogRef<ChartEnrollmentByScopeDialogComponent>,
		@Inject(MAT_DIALOG_DATA) data: ChartEnrollmentByScopeDialogData,
		snackBar: MatSnackBar
	) {
		super(dialogRef, data, snackBar);
	}

	ngOnInit(): void {
		this.form = this.fb.group({
			scopeModelId: [this.data.chart?.scopeModelId ?? null]
		});
	}

	onSave(): void {
		this.dialogRef.close({
			scopeModelId: this.form.value.scopeModelId
		});
	}
}
