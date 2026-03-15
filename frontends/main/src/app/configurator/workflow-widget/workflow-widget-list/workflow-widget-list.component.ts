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
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {LanguageService} from '../../services/language.service';
import {EmptyStateComponent} from '../../shared/empty-state/empty-state.component';
import {BaseListComponent} from '../../shared/base-list.component';
import {ListHeaderComponent} from '../../shared/list-header/list-header.component';
import {ModifiedDirective} from '../../shared/modified.directive';
import {WorkflowWidgetConfig} from '@core/model/workflow-widget-config';
import {WorkflowWidgetManagerService} from '../../services/manager/workflow-widget-manager.service';
import {WorkflowWidgetDetailComponent} from '../workflow-widget-detail/workflow-widget-detail.component';
import {WorkflowWidgetDialogService} from '../../services/dialogs/workflow-widget-dialog.service';

@Component({
	selector: 'app-workflow-widget-list',
	standalone: true,
	imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule,
		MatSnackBarModule, MatTooltip, WorkflowWidgetDetailComponent, EmptyStateComponent, ListHeaderComponent, ModifiedDirective],
	templateUrl: './workflow-widget-list.component.html',
	styleUrls: ['../../shared/list-shared.css']
})
export class WorkflowWidgetListComponent
	extends BaseListComponent<WorkflowWidgetConfig>
	implements OnInit, OnChanges, OnDestroy {
	@Input() override projectId = '';
	@Input() override project: ConfiguratorProject | null = null;
	@Input() override selectedNode: string | null = null;
	@Output() workflowWidgetsChanged = new EventEmitter<boolean>();
	@Output() workflowWidgetContextChanged = new EventEmitter<{
		workflowWidgets: any[];
		selectedWorkflowWidgetId: string | null;
	}>();

	constructor(
		public workflowWidgetManager: WorkflowWidgetManagerService,
		public override languageService: LanguageService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowWidgetDialogService: WorkflowWidgetDialogService,
		snackBar: MatSnackBar
	) {
		super(workflowWidgetManager, languageService, snackBar);
	}

	getEntityId(ww: WorkflowWidgetConfig): string {return ww.workflowWidgetId;}
	getNodePrefix(): string {return 'workflowWidget';}
	getListNodeName(): string {return 'workflowWidgets';}

	get workflowWidgets(): WorkflowWidgetConfig[] {return this.manager.getAll();}
	get selectedWorkflowWidget(): WorkflowWidgetConfig | null {return this.selected as WorkflowWidgetConfig | null;}
	get modifiedWorkflowWidgetIds(): Set<string> {return this.manager.getModifiedIds();}
	get originalWorkflowWidgets(): WorkflowWidgetConfig[] {return this.manager.getOriginals();}

	loadWorkflowWidgets(): void {this.load();}
	load(): void {
		this.loading = true;
		forkJoin({
			workflowWidgets: this.workflowWidgetManager.load(this.projectId),
			workflowStates: this.workflowStateManager.isLoaded()
				? of(null)
				: this.workflowStateManager.load(this.projectId)
		}).subscribe({
			next: ({workflowWidgets}) => this.afterLoad(workflowWidgets),
			error: (e: HttpErrorResponse) => this.handleLoadError(e, 'workflowWidgets')
		});
	}

	emitChangedEvent(hasModifications: boolean): void {
		this.workflowWidgetsChanged.emit(hasModifications);
	}

	emitContextEvent(): void {
		this.workflowWidgetContextChanged.emit({
			workflowWidgets: [...this.workflowWidgets],
			selectedWorkflowWidgetId: this.selected?.workflowWidgetId || null
		});
	}

	onCreate(): void {
		this.workflowWidgetDialogService.openCreateDialog(this.projectId, this.projectLanguages)
			.subscribe((result: WorkflowWidgetConfig | null) => {
				if(result) {
					this.workflowWidgetManager.create(this.projectId, result).subscribe({
						next: () => this.afterCreate('WorkflowWidget'),
						error: e => {
							console.error(e);
							this.snackBar.open('Failed to create workflow widget', 'Close', {duration: 3000});
						}
					});
				}
			});
	}

	onUpdated(updated: WorkflowWidgetConfig): void {
		this.workflowWidgetManager.update(updated);
		this.selected = this.workflowWidgetManager.getById(updated.workflowWidgetId) || null;
		this.emitModificationChange();
	}

	onDeleted(workflowWidgetId: string): void {
		const workflowWidget = this.workflowWidgets.find(ww => ww.workflowWidgetId === workflowWidgetId);
		if(!workflowWidget) {
			return;
		}
		this.workflowWidgetManager.delete(this.projectId, workflowWidgetId).subscribe({
			next: () => this.afterDelete(workflowWidget, 'WorkflowWidget'),
			error: (e: HttpErrorResponse) => {
				console.error(e);
				this.snackBar.open('Failed to delete workflow widget', 'Close', {duration: 3000});
			}
		});
	}

	onSelectWorkflowWidget(ww: WorkflowWidgetConfig): void {this.onSelect(ww);}
	onCreateWorkflowWidget(): void {this.onCreate();}
	onWorkflowWidgetUpdated(ww: WorkflowWidgetConfig): void {this.onUpdated(ww);}
	onWorkflowWidgetDeleted(id: string): void {this.onDeleted(id);}

	getWorkflowLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowManager.getById(i));
	}

	getWorkflowStateLabel(id: string): string {
		return this.languageService.getLabelById(id, i => this.workflowStateManager.getById(i));
	}
}
