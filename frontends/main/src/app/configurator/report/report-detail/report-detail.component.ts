import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin, of, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ProjectLanguage} from '@core/model/project-language';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';
import {Report} from '@core/model/report';
import {ReportManagerService} from '../../services/manager/report-manager.service';
import {ReportDialogService} from '../../services/dialogs/report-dialog.service';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {FieldModelManagerService} from '../../services/manager/field-model-manager.service';

@Component({
	selector: 'app-report-detail',
	standalone: true,
	templateUrl: './report-detail.component.html',
	styleUrls: ['../../shared/detail-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DangerZoneComponent]
})
export class ReportDetailComponent implements OnInit, OnDestroy {
	@Input() report!: Report;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allReports: Report[] = [];
	@Output() reportUpdated = new EventEmitter<Report>();
	@Output() reportDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[];
	private languageSubscription: Subscription;

	constructor(
		public reportManager: ReportManagerService,
		private workflowManager: WorkflowManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private fieldModelManager: FieldModelManagerService,
		public languageService: LanguageService,
		private reportDialogService: ReportDialogService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});

		this.loadFieldModels();
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	private loadFieldModels(): void {
		forkJoin({
			fieldModels: this.fieldModelManager.isLoaded()
				? of(null)
				: this.fieldModelManager.load(this.projectId)
		}).subscribe({
			error: error => {
				console.error('Error loading field models', error);
				this.snackBar.open('Failed to load field models', 'Close', {duration: 3000});
			}
		});
	}

	onEditBasicInfo(): void {
		this.reportDialogService.openBasicInfoDialog(
			this.projectId,
			this.report,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedReport: Report = {...this.report, ...result};
				this.reportManager.update(updatedReport);
				this.reportUpdated.emit(updatedReport);
				this.showStagedMessage();
			}
		});
	}

	onEditResources(): void {
		const currentDraft = this.reportManager.getById(this.report.reportId);
		const draft = currentDraft || this.report;

		this.reportDialogService.openResourcesDialog(
			this.projectId,
			draft
		).subscribe(result => {
			if(result) {
				const draft = this.reportManager.getById(this.report.reportId);
				if(draft) {
					Object.assign(draft, result);
					this.reportManager.update(draft);
					this.reportUpdated.emit(draft);
					this.showStagedMessage();
				}
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Report',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.report.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});
		dialogRef.afterClosed().subscribe(confirmed => {
			if(confirmed) {
				this.reportDeleted.emit(this.report.reportId);
			}
		});
	}

	onClose(): void {
		this.closed.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.reportManager.isFieldModified(this.report.reportId, fieldName);
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
