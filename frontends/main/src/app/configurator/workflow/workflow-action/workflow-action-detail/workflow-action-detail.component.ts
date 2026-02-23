import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {LanguageService} from '../../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../../../confirmation-dialog/confirmation-dialog.component';
import {WorkflowAction} from '@core/model/workflow-action';
import {Workflow} from '@core/model/workflow';
import {WorkflowActionDialogService} from '../../../services/dialogs/workflow-action-dialog.service';

@Component({
	selector: 'app-workflow-action-detail',
	standalone: true,
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
	],
	templateUrl: './workflow-action-detail.component.html',
	styleUrls: ['../../../shared/detail-shared.css']
})
export class WorkflowActionDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() workflowActionId: string | null = null;
	@Input() workflowActions: WorkflowAction[] = [];
	@Input() originalWorkflowActions: WorkflowAction[] = [];
	@Input() workflow: Workflow | null = null;
	@Input() project: ConfiguratorProject | null = null;

	@Output() closed = new EventEmitter<void>();
	@Output() workflowActionUpdated = new EventEmitter<any>();
	@Output() workflowActionDeleted = new EventEmitter<string>();

	draftWorkflowAction: WorkflowAction | null = null;
	originalWorkflowAction: WorkflowAction | null = null;

	selectedLanguage = '';
	private languageSubscription: Subscription;

	availableLanguages: {code: string; name: string; isDefault: boolean}[] = [];

	constructor(
		public languageService: LanguageService,
		private workflowActionDialogService: WorkflowActionDialogService,
		private dialog: MatDialog
	) {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnInit(): void {
		this.loadLanguages();
		this.loadWorkflowAction();
	}

	ngOnChanges(changes: SimpleChanges): void {
		if(changes['workflowActionId'] || changes['workflowActions']) {
			this.loadWorkflowAction();
		}

		if(changes['project']) {
			this.loadLanguages();
		}
	}

	ngOnDestroy(): void {
		this.languageSubscription?.unsubscribe();
	}

	private loadLanguages(): void {
		if(this.project?.languages) {
			this.availableLanguages = this.project.languages.map(lang => ({
				code: lang.languageCode || '',
				name: this.languageService.getLanguageName(lang.languageCode),
				isDefault: lang.isDefault || false
			}));
		}
	}

	private loadWorkflowAction(): void {
		if(!this.workflowActionId) {
			this.draftWorkflowAction = null;
			this.originalWorkflowAction = null;
			return;
		}

		const draft = this.workflowActions.find(wfa => wfa.workflowActionId === this.workflowActionId);
		const original = this.originalWorkflowActions.find(wfa => wfa.workflowActionId === this.workflowActionId);

		if(draft) {
			this.draftWorkflowAction = draft;
			this.originalWorkflowAction = original || null;
		}
	}

	isFieldModified(field: keyof WorkflowAction): boolean {
		if(!this.originalWorkflowAction || !this.draftWorkflowAction) {
			return false;
		}
		return JSON.stringify(this.originalWorkflowAction[field]) !== JSON.stringify(this.draftWorkflowAction[field]);
	}

	onClose(): void {
		this.closed.emit();
	}

	onEditBasicInfo(): void {
		if(!this.draftWorkflowAction) {
			return;
		}

		const workflowId = this.draftWorkflowAction.workflowId || this.workflow?.workflowId;

		if(!workflowId) {
			console.error('No workflow ID available');
			return;
		}

		this.workflowActionDialogService.openEditDialog(
			this.projectId,
			workflowId,
			this.draftWorkflowAction,
			this.project?.languages || []
		).subscribe((result: any) => {
			if(result && this.draftWorkflowAction) {
				const updatedWorkflowAction: WorkflowAction = {
					...this.draftWorkflowAction,
					...result
				};

				this.workflowActionUpdated.emit(updatedWorkflowAction);
			}
		});
	}

	onEditDocumentation(): void {
		if(!this.draftWorkflowAction) {
			return;
		}

		this.workflowActionDialogService.openDocumentationDialog(
			this.draftWorkflowAction,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.draftWorkflowAction) {
				this.draftWorkflowAction = {...this.draftWorkflowAction, ...result};
				this.workflowActionUpdated.emit(this.draftWorkflowAction);
			}
		});
	}

	onEditSignature(): void {
		if(!this.draftWorkflowAction) {
			return;
		}

		this.workflowActionDialogService.openSignatureDialog(
			this.draftWorkflowAction,
			this.project?.languages || []
		).subscribe(result => {
			if(result && this.draftWorkflowAction) {
				this.draftWorkflowAction = {...this.draftWorkflowAction, ...result};
				this.workflowActionUpdated.emit(this.draftWorkflowAction);
			}
		});
	}

	onDelete(): void {
		if(!this.draftWorkflowAction) {
			return;
		}

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow Action',
				message: `Are you sure you want to delete "${this.languageService.getTranslatedValue(this.draftWorkflowAction.shortname)}"? This action cannot be undone.`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed && this.draftWorkflowAction) {
				this.workflowActionDeleted.emit(this.draftWorkflowAction.workflowActionId);
			}
		});
	}
}
