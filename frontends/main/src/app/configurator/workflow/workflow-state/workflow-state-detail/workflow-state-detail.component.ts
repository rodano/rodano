import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowState} from '@core/model/workflow-state';
import {Workflow} from '@core/model/workflow';
import {WorkflowStateDialogService} from '../../../services/dialogs/workflow-state-dialog.service';
import {WorkflowStateManagerService} from '../../../services/manager/workflow-state-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {DangerZoneComponent} from '../../../shared/danger-zone/danger-zone.component';

@Component({
	selector: 'app-workflow-state-detail',
	standalone: true,
	templateUrl: './workflow-state-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule,
		DangerZoneComponent
	]
})
export class WorkflowStateDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() workflowStateId = '';
	@Input() workflow: Workflow | null = null;
	@Input() project: ConfiguratorProject | null = null;
	@Output() closed = new EventEmitter<void>();
	@Output() workflowStateUpdated = new EventEmitter<any>();
	@Output() workflowStateDeleted = new EventEmitter<string>();

	originalWorkflowState: WorkflowState | null = null;
	draftWorkflowState: WorkflowState | null = null;
	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	constructor(
		public languageService: LanguageService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowStateManager: WorkflowStateManagerService,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
		this.loadWorkflowState();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['workflowStateId']) {
			this.loadWorkflowState();
		}
	}

	ngOnDestroy(): void {
		if(this.languageSubscription) {
			this.languageSubscription.unsubscribe();
		}
	}

	private loadWorkflowState(): void {
		const workflowState = this.workflowStateManager.getById(this.workflowStateId);
		this.draftWorkflowState = workflowState ? JSON.parse(JSON.stringify(workflowState)) : null;
		const original = this.workflowStateManager.getOriginals().find(wfs => wfs.workflowStateId === this.workflowStateId);
		this.originalWorkflowState = original ? JSON.parse(JSON.stringify(original)) : null;
	}

	isFieldModified(field: keyof WorkflowState): boolean {
		if(!this.originalWorkflowState || !this.draftWorkflowState) {
			return false;
		}
		return JSON.stringify(this.originalWorkflowState[field]) !== JSON.stringify(this.draftWorkflowState[field]);
	}

	onClose(): void {
		this.closed.emit();
	}

	get hasAggregation(): boolean {
		return !!this.workflow?.aggregatedWorkflowId;
	}

	onEditBasicInfo(): void {
		if(!this.draftWorkflowState || !this.workflow) {
			return;
		}

		this.workflowStateDialogService.openBasicInfoDialog(
			this.draftWorkflowState,
			this.projectId,
			this.workflow.workflowId,
			this.projectLanguages
		).subscribe(result => {
			if(result && this.draftWorkflowState) {
				this.draftWorkflowState = {
					...this.draftWorkflowState,
					...result
				};
				this.workflowStateUpdated.emit(this.draftWorkflowState);
			}
		});
	}

	onEditAggregation(): void {
		if(!this.draftWorkflowState || !this.workflow?.aggregatedWorkflowId) {
			return;
		}

		this.workflowStateDialogService.openAggregationDialog(
			this.draftWorkflowState,
			this.workflow.aggregatedWorkflowId
		).subscribe(result => {
			if(result && this.draftWorkflowState) {
				this.draftWorkflowState = {...this.draftWorkflowState, ...result};
				this.workflowStateUpdated.emit(this.draftWorkflowState);
			}
		});
	}

	onEditPossibleActions(): void {
		if(!this.draftWorkflowState || !this.workflow) {
			return;
		}

		this.workflowStateDialogService.openActionsDialog(
			this.draftWorkflowState,
			this.workflow.workflowId
		).subscribe(result => {
			if(result && this.draftWorkflowState) {
				this.draftWorkflowState = {...this.draftWorkflowState, ...result};
				this.workflowStateUpdated.emit(this.draftWorkflowState);
			}
		});
	}

	onDelete(): void {
		if(!this.draftWorkflowState) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow State',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftWorkflowState.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftWorkflowState) {
				this.workflowStateDeleted.emit(this.draftWorkflowState.workflowStateId);
			}
		});
	}

	getWorkflowStateLabel(workflowStateId: string): string {
		return this.languageService.getLabelById(workflowStateId, id => this.workflowStateManager.getById(id));
	}
}
