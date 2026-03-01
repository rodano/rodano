import {Component, Inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatIconModule} from '@angular/material/icon';
import {LanguageService} from '../../../services/language.service';
import {Report} from '@core/model/report';
import {FieldModel} from '@core/model/field-model';

export interface ReportResourcesDialogData {
	report: Report;
	availableFieldModels: FieldModel[];
}

@Component({
	selector: 'app-report-resources-dialog',
	standalone: true,
	imports: [
		CommonModule,
		MatDialogModule,
		MatButtonModule,
		MatIconModule,
		MatSelectModule
	],
	templateUrl: './report-resources-dialog.component.html',
	styleUrls: ['../../dialog-shared.css']
})
export class ReportResourcesDialogComponent implements OnInit {
	availableFieldModels: FieldModel[] = [];
	selectedFieldModels: FieldModel[] = [];

	constructor(
		public languageService: LanguageService,
		private dialogRef: MatDialogRef<ReportResourcesDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: ReportResourcesDialogData
	) {}

	ngOnInit(): void {
		this.initializeFieldModels();
	}

	private initializeFieldModels(): void {
		const selectedIds = this.data.report.fieldModelIds || [];
		this.selectedFieldModels = this.data.availableFieldModels.filter(fm => selectedIds.includes(fm.fieldModelId));
		this.availableFieldModels = this.data.availableFieldModels.filter(fm => !selectedIds.includes(fm.fieldModelId));
	}

	onAddFieldModel(fieldModel: FieldModel): void {
		this.availableFieldModels = this.availableFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.selectedFieldModels = [...this.selectedFieldModels, fieldModel];
	}

	onRemoveFieldModel(fieldModel: FieldModel): void {
		this.selectedFieldModels = this.selectedFieldModels.filter(fm => fm.fieldModelId !== fieldModel.fieldModelId);
		this.availableFieldModels = [...this.availableFieldModels, fieldModel];
	}

	onSave(): void {
		const result: any = {};

		const originalFormIds = [...(this.data.report.fieldModelIds || [])].sort();
		const currentFormIds = [...this.selectedFieldModels.map(fm => fm.fieldModelId)].sort();
		if(JSON.stringify(originalFormIds) !== JSON.stringify(currentFormIds)) {
			result.fieldModelIds = this.selectedFieldModels.map(fm => fm.fieldModelId);
		}

		this.dialogRef.close(result);
	}

	onCancel(): void {
		this.dialogRef.close(null);
	}
}
