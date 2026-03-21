import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {forkJoin} from 'rxjs';
import {LanguageService} from '../../services/language.service';
import {HttpErrorResponse} from '@angular/common/http';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {Report} from '@core/model/report';
import {ReportManagerService} from '../../services/manager/report-manager.service';
import {ReportDialogService} from '../../services/dialogs/report-dialog.service';
import {ReportDetailComponent} from '../report-detail/report-detail.component';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {Profile} from '@core/model/profile';
import {ReportGrantsMatrixComponent} from '../report-grants-matrix/report-grants-matrix.component';

@Component({
	selector: 'app-report-list',
	standalone: true,
	templateUrl: './report-list.component.html',
	styleUrls: ['../../shared/list-shared.css'],
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule,
		ReportDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective, ReportGrantsMatrixComponent]
})
export class ReportListComponent
	extends BaseListComponent<Report>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() reportsChanged = new EventEmitter<boolean>();
	@Output() reportContextChanged = new EventEmitter<{
		reports: any[];
		selectedReportId: string | null;
	}>();

	showMatrix = false;

	constructor(
		public reportManager: ReportManagerService,
		public override languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private datasetModelManager: DatasetModelManagerService,
		private profileManager: ProfileManagerService,
		private reportDialogService: ReportDialogService,
		snackBar: MatSnackBar
	) {
		super(reportManager, languageService, snackBar);
	}

	getEntityId(r: Report): string {return r.reportId;}
	getNodePrefix(): string {return 'report';}
	getListNodeName(): string {return 'reports';}

	get reports(): Report[] {return this.reportManager.getAll();}
	get selectedReport(): Report | null {return this.selected as Report | null;}
	get modifiedReportIds(): Set<string> {return this.reportManager.getModifiedIds();}
	get originalReports(): Report[] {return this.reportManager.getOriginals();}

	get profiles(): Profile[] {return this.profileManager.getAll();}

	loadReports(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			reports: this.reportManager.load(this.projectId)
		}).subscribe({
			next: ({reports}) => this.afterLoad(reports),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'reports')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.reportsChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.reportContextChanged.emit({
			reports: [...this.reports],
			selectedReportId: this.selected?.reportId || null
		});
	}

	onCreate(): void {
		this.reportDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: Report | null) => {
				if(result) {
					this.reportManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Report'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create report', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: Report): void {
		this.reportManager.update(updated);
		this.selected = this.reportManager.getById(updated.reportId) || null;
		this.emitModificationChange();
	}

	onDeleted(reportId: string): void {
		const report = this.reports.find(r => r.reportId === reportId);
		if(!report) {
			return;
		}
		this.reportManager.delete(this.projectId, report.reportId).subscribe({
			next: () => this.afterDelete(report, 'Report'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete report', 'Close', {duration: 3000});
			}
		});
	}

	onSelectReport(r: Report): void {this.onSelect(r);}
	onCreateReport(): void {this.onCreate();}
	onReportUpdated(r: Report): void {this.onUpdated(r);}
	onReportDeleted(id: string): void {this.onDeleted(id);}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	onToggleMatrix(): void {
		this.showMatrix = !this.showMatrix;
		if(this.showMatrix) {
			this.selected = null;
		}
	}
}
