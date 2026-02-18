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

@Component({
	selector: 'app-workflow-state-detail',
	standalone: true,
	templateUrl: './workflow-state-detail.component.html',
	styleUrls: ['./workflow-state-detail.component.css'],
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
	private languageSubscription: Subscription;

	constructor(
		private languageService: LanguageService,
		private workflowStateDialogService: WorkflowStateDialogService,
		private workflowStateManager: WorkflowStateManagerService,
		private dialog: MatDialog
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});

		this.workflowStateManager.loadFull(this.projectId).subscribe({
			next: () => {
				this.loadWorkflowState();
			},
			error: error => console.error('Error loading full workflow states:', error)
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['workflowStateId'] && this.workflowStateId) {
			this.workflowStateManager.loadFull(this.projectId).subscribe({
				next: () => {
					this.loadWorkflowState();
				},
				error: error => console.error('Error loading full workflow state:', error)
			});
		}
		else if(changes['workflowStates']) {
			this.loadWorkflowState();
		}
	}

	ngOnDestroy(): void {
		if(this.languageSubscription) {
			this.languageSubscription.unsubscribe();
		}
	}

	private loadWorkflowState(): void {
		const workflowState = this.workflowStates.find(wfs => wfs.workflowStateId === this.workflowStateId);
		this.draftWorkflowState = workflowState ? JSON.parse(JSON.stringify(workflowState)) : null;

		const original = this.originalWorkflowStates.find(wfs => wfs.workflowStateId === this.workflowStateId);
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

	onEditBasicInfo(): void {
		if(!this.draftWorkflowState || !this.workflow) {
			return;
		}

		this.workflowStateDialogService.openBasicInfoDialog(
			this.draftWorkflowState,
			this.projectId,
			this.workflow.workflowId,
			this.project?.languages || []
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
