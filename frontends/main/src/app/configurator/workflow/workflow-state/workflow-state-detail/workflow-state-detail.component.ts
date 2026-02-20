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
import {WorkflowActionManagerService} from '../../../services/manager/workflow-action-manager.service';
import {ProjectLanguage} from '@core/model/project-language';

@Component({
	selector: 'app-workflow-state-detail',
	standalone: true,
	templateUrl: './workflow-state-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule]
})
export class WorkflowStateDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() workflowStateId = '';
	@Input() workflowStates: WorkflowState[] = [];
	@Input() originalWorkflowStates: WorkflowState[] = [];
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
		private languageService: LanguageService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowStateManager: WorkflowStateManagerService,
		private workflowActionManager: WorkflowActionManagerService,
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
		if(changes['workflowStateId'] || changes['workflowStates']) {
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

	getTranslatedValue(translations: Record<string, string> | undefined): string {
		if(!translations) {
			return '';
		}
		return translations[this.selectedLanguage] || '';
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (error) {
			console.error(error);
			return code.toUpperCase();
		}
	}

	getWorkflowStateName(workflowStateId: string): string {
		const wfs = this.workflowStates.find(wf => wf.workflowStateId === workflowStateId);
		if(!wfs) {
			return workflowStateId;
		}

		const name = this.languageService.getDefaultTranslation(wfs.shortname) || wfs.id;
		return `${name} (${wfs.id})`;
	}

	getWorkflowStateCode(workflowStateId: string): string {
		const workflowState = this.workflowStates.find(wfs => wfs.workflowStateId === workflowStateId);
		if(!workflowState) {
			return workflowStateId;
		}

		const shortname = this.languageService.getDefaultTranslation(workflowState.shortname) || workflowState.id;
		return `${shortname} (${workflowState.id})`;
	}

	get hasAggregation(): boolean {
		return !!this.workflow?.aggregatedWorkflowId;
	}

	get aggregatedWorkflowStates(): {id: string; name: string; code: string}[] {
		if(!this.workflow?.aggregatedWorkflowId) {
			return [];
		}
		return this.workflowStateManager
			.getAllForWorkflow(this.workflow.aggregatedWorkflowId)
			.map(wfs => ({
				id: wfs.workflowStateId,
				name: this.languageService.getDefaultTranslation(wfs.shortname) || wfs.id,
				code: wfs.id
			}));
	}

	getAggregatedStateLabel(workflowStateId: string | undefined): string {
		if(!workflowStateId) {
			return 'Not set';
		}
		const workflowState = this.aggregatedWorkflowStates.find(s => s.id === workflowStateId);
		return workflowState ? `${workflowState.name} (${workflowState.code})` : workflowStateId;
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
		if(!this.draftWorkflowState) {
			return;
		}

		this.workflowStateDialogService.openAggregationDialog(
			this.draftWorkflowState,
			this.aggregatedWorkflowStates
		).subscribe(result => {
			if(result && this.draftWorkflowState) {
				this.draftWorkflowState = {...this.draftWorkflowState, ...result};
				this.workflowStateUpdated.emit(this.draftWorkflowState);
			}
		});
	}

	get availableActionsForDialog(): {id: string; name: string; code: string}[] {
		if(!this.workflow) {
			return [];
		}
		return this.workflowActionManager
			.getAllForWorkflow(this.workflow.workflowId)
			.map(wfa => ({
				id: wfa.workflowActionId,
				name: this.languageService.getDefaultTranslation(wfa.shortname) || wfa.id,
				code: wfa.id
			}));
	}

	onEditPossibleActions(): void {
		if(!this.draftWorkflowState) {
			return;
		}

		this.workflowStateDialogService.openActionsDialog(
			this.draftWorkflowState,
			this.availableActionsForDialog
		).subscribe(result => {
			if(result && this.draftWorkflowState) {
				const possibleActions = (result.possibleActionIds as string[])
					.map(id => this.workflowActionManager.getById(id))
					.filter(a => !!a);
				this.draftWorkflowState = {...this.draftWorkflowState, possibleActions};
				this.workflowStateUpdated.emit(this.draftWorkflowState);
			}
		});
	}

	onDelete(): void {
		if(!this.draftWorkflowState) {
			return;
		}

		const workflowStateName = this.getTranslatedValue(this.draftWorkflowState.shortname) || this.draftWorkflowState.id;

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow State',
				message: `Are you sure you want to delete "${workflowStateName}"? This action cannot be undone.`,
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
}
