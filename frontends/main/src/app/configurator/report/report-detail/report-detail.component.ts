import {Component, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {Report} from '@core/model/report';
import {ReportManagerService} from '../../services/manager/report-manager.service';
import {ReportDialogService} from '../../services/dialogs/report-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';
import {BaseManagerDetailComponent} from '../../shared/base-manager-detail.component';
import {SettingItemComponent} from '../../shared/setting-item/setting-item.component';

@Component({
	selector: 'app-report-detail',
	standalone: true,
	templateUrl: './report-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent, SettingItemComponent]
})
export class ReportDetailComponent extends BaseManagerDetailComponent<Report, ReportManagerService> {
	@Input() override entity!: Report;
	@Input() override allEntities: Report[] = [];
	@Output() reportUpdated = this.entityUpdated;
	@Output() reportDeleted = this.entityDeleted;

	@Input() set report(v: Report) {this.entity = v;}
	get report(): Report {return this.entity;}

	@Input() set allReports(v: Report[]) {this.allEntities = v;}

	constructor(
		reportManager: ReportManagerService,
		private workflowManager: WorkflowManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		languageService: LanguageService,
		private reportDialogService: ReportDialogService,
		private dialog: MatDialog,
		snackBar: MatSnackBar
	) {
		super(reportManager, languageService, snackBar);
	}

	protected getEntityId(): string {return this.entity.reportId;}

	protected override onInit(): void {
		this.loadFieldModels();
	}

	private loadFieldModels(): void {
		if(this.fieldModelManager.isLoaded()) {
			return;
		}
		this.fieldModelManager.load(this.projectId).subscribe({
			error: error => {
				console.error('Error loading field models', error);
				this.snackBar.open('Failed to load field models', 'Close', {duration: 3000});
			}
		});
	}

	onEditBasicInfo(): void {
		this.reportDialogService.openBasicInfoDialog(
			this.projectId,
			this.entity,
			this.projectLanguages
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onEditResources(): void {
		this.reportDialogService.openResourcesDialog(
			this.projectId,
			this.entity
		).subscribe(result => {
			if(result) {
				this.applyUpdate({...this.entity, ...result});
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Report',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.entity.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.entityDeleted.emit(this.entity.reportId);
			}
		});
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	getFieldModelLabel(fieldModelId: string): string {
		return this.languageService.getLabelById(fieldModelId, id => this.fieldModelManager.getById(id));
	}
}
