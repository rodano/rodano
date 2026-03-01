import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {ProjectLanguage} from '@core/model/project-language';
import {forkJoin, Subscription} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {Report} from '@core/model/report';
import {ReportManagerService} from '../../services/manager/report-manager.service';
import {ReportDialogService} from '../../services/dialogs/report-dialog.service';
import {ReportDetailComponent} from '../report-detail/report-detail.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';

@Component({
	selector: 'app-report-list',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatProgressSpinnerModule,
		MatSnackBarModule,
		ReportDetailComponent,
		EmptyStateComponent
	],
	templateUrl: './report-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class ReportListComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() selectedNode: string | null = null;
	@Output() nodeSelected = new EventEmitter<string | null>();
	@Output() reportsChanged = new EventEmitter<{modificationCount: number}>();
	@Output() reportContextChanged = new EventEmitter<{
		reports: any[];
		selectedReportId: string | null;
	}>();

	selectedReport: Report | null = null;
	viewMode = 'detail';
	loading = false;

	projectLanguages: ProjectLanguage[] = [];
	selectedLanguage = '';
	private languageSubscription: Subscription;

	constructor(
		public reportManager: ReportManagerService,
		public languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private reportDialogService: ReportDialogService,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.loadReports();

		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnChanges(changes: any): void {
		if(changes['selectedNode'] && this.reports.length > 0) {
			const nodeId = this.selectedNode;
			if(nodeId?.startsWith('report-')) {
				const reportId = nodeId.replace('report-', '');
				const report = this.reports.find(r => r.reportId === reportId);
				if(report) {
					this.selectedReport = report;
				}
			}
			else if(nodeId === 'reports') {
				this.selectedReport = null;
			}
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	get viewLevel(): number {
		if(!this.selectedReport) {
			return 0;
		}
		return 2;
	}

	get reports(): Report[] {
		return this.reportManager.getAll();
	}

	get modifiedReportIds(): Set<string> {
		return this.reportManager.getModifiedIds();
	}

	get originalReports(): Report[] {
		return this.reportManager.getOriginals();
	}

	get totalModificationCount(): number {
		return this.reportManager.getModificationCount();
	}

	loadReports(): void {
		this.loading = true;
		forkJoin({
			reports: this.reportManager.load(this.projectId)
		}).subscribe({
			next: ({reports}) => {
				if(this.selectedReport) {
					this.selectedReport = reports.find(
						r => r.reportId === this.selectedReport!.reportId
					) || null;
				}
				this.loading = false;
				this.emitContext();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error loading reports:', error);
				this.snackBar.open('Failed to load reports', 'Close', {duration: 3000});
				this.loading = false;
			}
		});
	}

	onSelectReport(report: Report): void {
		if(this.selectedReport?.reportId === report.reportId) {
			this.clearSelection();
		}
		else {
			this.selectReport(report);
		}
		this.emitContext();
	}

	clearSelection(): void {
		this.selectedReport = null;
		this.viewMode = 'detail';
		this.nodeSelected.emit('report');
	}

	private selectReport(report: Report): void {
		const previousReportId = this.selectedReport?.reportId;
		this.selectedReport = report;

		if(previousReportId !== report.reportId) {
			this.viewMode = 'detail';
		}
		this.emitContext();
		this.nodeSelected.emit(`report-${report.reportId}`);
	}

	isSelected(report: Report): boolean {
		return this.selectedReport?.reportId === report.reportId;
	}

	onCreateReport(): void {
		this.reportDialogService.openCreateDialog(
			this.projectId,
			this.projectLanguages
		).subscribe((result: Report | null) => {
			if(result) {
				this.reportManager.create(this.projectId, result).subscribe({
					next: () => {
						this.snackBar.open('Report created', 'Close', {duration: 2000});
						this.loadReports();
						this.emitModificationChange();
					},
					error: error => {
						console.error('Error creating report', error);
						this.snackBar.open('Failed to create report', 'Close', {duration: 3000});
					}
				});
			}
		});
	}

	onReportUpdated(updatedReport: Report): void {
		this.selectedReport = this.reportManager.getById(updatedReport.reportId) || null;
		this.emitModificationChange();
	}

	onReportDeleted(reportId: string): void {
		const report = this.reports.find(r => r.reportId === reportId);
		if(!report) {
			return;
		}
		this.performDelete(report);
	}

	private performDelete(report: Report): void {
		this.reportManager.delete(this.projectId, report.reportId).subscribe({
			next: () => {
				this.snackBar.open('Report deleted', 'Close', {duration: 2000});

				if(this.selectedReport?.reportId === report.reportId) {
					this.clearSelection();
				}

				this.loadReports();
				this.emitModificationChange();
			},
			error: (error: HttpErrorResponse) => {
				console.error('Error deleting report', error);
				this.snackBar.open('Failed to delete report', 'Close', {duration: 3000});
			}
		});
	}

	private emitModificationChange(): void {
		this.reportsChanged.emit({modificationCount: this.totalModificationCount});
	}

	private emitContext(): void {
		this.reportContextChanged.emit({
			reports: [...this.reports],
			selectedReportId: this.selectedReport?.reportId || null
		});
	}

	isModified(reportId: string): boolean {
		return this.reportManager.isModified(reportId);
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}
}
