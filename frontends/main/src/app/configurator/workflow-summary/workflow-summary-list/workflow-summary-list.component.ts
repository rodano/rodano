import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTooltip} from '@angular/material/tooltip';
import {HttpErrorResponse} from '@angular/common/http';
import {forkJoin, of} from 'rxjs';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {LanguageService} from '../../services/language.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {WorkflowSummary} from '@core/model/workflow-summary';
import {WorkflowSummaryManagerService} from '../../services/manager/workflow-summary-manager.service';
import {EventModelManagerService} from '../../services/manager/event-model-manager.service';
import {WorkflowSummaryDetailComponent} from '../workflow-summary-detail/workflow-summary-detail.component';
import {WorkflowSummaryDialogService} from '../../services/dialogs/workflow-summary-dialog.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';

@Component({
	selector: 'app-workflow-summary-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, MatTooltip, WorkflowSummaryDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './workflow-summary-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class WorkflowSummaryListComponent
	extends BaseListComponent<WorkflowSummary>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() workflowSummariesChanged = new EventEmitter<boolean>();
	@Output() workflowSummaryContextChanged = new EventEmitter<{
		workflowSummaries: any[];
		selectedWorkflowSummaryId: string | null;
	}>();

	constructor(
		public workflowSummaryManager: WorkflowSummaryManagerService,
		public override languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private eventModelManager: EventModelManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowSummaryDialogService: WorkflowSummaryDialogService,
		snackBar: MatSnackBar
	) {
		super(workflowSummaryManager, languageService, snackBar);
	}

	getEntityId(ws: WorkflowSummary): string {return ws.workflowSummaryId;}
	getNodePrefix(): string {return 'workflowSummary';}
	getListNodeName(): string {return 'workflowSummaries';}

	get workflowSummaries(): WorkflowSummary[] {return this.manager.getAll();}
	get selectedWorkflowSummary(): WorkflowSummary | null {return this.selected as WorkflowSummary | null;}
	get modifiedWorkflowSummaryIds(): Set<string> {return this.manager.getModifiedIds();}
	get originalWorkflowSummaries(): WorkflowSummary[] {return this.manager.getOriginals();}

	loadWorkflowSummaries(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			workflowSummaries: this.workflowSummaryManager.load(this.projectId),
			eventModels: this.eventModelManager.isLoaded()
				? of(null)
				: this.eventModelManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({workflowSummaries}) => this.afterLoad(workflowSummaries),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'workflowSummaries')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.workflowSummariesChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.workflowSummaryContextChanged.emit({
			workflowSummaries: [...this.workflowSummaries],
			selectedWorkflowSummaryId: this.selected?.workflowSummaryId || null
		});
	}

	onCreate(): void {
		this.workflowSummaryDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: WorkflowSummary | null) => {
				if(result) {
					this.workflowSummaryManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('Workflow summary'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create workflow summary', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: WorkflowSummary): void {
		this.workflowSummaryManager.update(updated);
		this.selected = this.workflowSummaryManager.getById(updated.workflowSummaryId) || null;
		this.emitModificationChange();
	}

	onDeleted(workflowSummaryId: string): void {
		const workflowSummary = this.workflowSummaries.find(ws => ws.workflowSummaryId === workflowSummaryId);
		if(!workflowSummary) {
			return;
		}
		this.workflowSummaryManager.delete(this.projectId, workflowSummaryId).subscribe({
			next: () => this.afterDelete(workflowSummary, 'WorkflowSummary'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete workflow summary', 'Close', {duration: 3000});
			}
		});
	}

	onSelectWorkflowSummary(ws: WorkflowSummary): void {this.onSelect(ws);}
	onCreateWorkflowSummary(): void {this.onCreate();}
	onWorkflowSummaryUpdated(ws: WorkflowSummary): void {this.onUpdated(ws);}
	onWorkflowSummaryDeleted(id: string): void {this.onDeleted(id);}

	getWorkflowLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowManager.getById(i));
	}

	getEventModelLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.eventModelManager.getById(i));
	}
}
