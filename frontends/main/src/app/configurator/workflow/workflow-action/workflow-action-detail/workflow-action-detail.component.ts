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
	styleUrls: ['./workflow-action-detail.component.css']
})
export class WorkflowActionDetailComponent implements OnInit, OnChanges, OnDestroy {
	@Input() projectId = '';
	@Input() workflowActionId: string | null = null;
	@Input() workflowActions: WorkflowAction[] = [];
	@Input() originalWorkflowActions: WorkflowAction[] = [];
	@Input() workflow: Workflow | null = null;
	@Input() project: ConfiguratorProject | null = null;

	@Output() closed = new EventEmitter<void>();
	@Output() workflowActionUpdated = new EventEmitter<WorkflowAction>();
	@Output() workflowActionDeleted = new EventEmitter<string>();

	draftWorkflowAction: WorkflowAction | null = null;
	originalWorkflowAction: WorkflowAction | null = null;
	modifiedFields = new Set<string>();

	selectedLanguage = '';
	private languageSubscription: Subscription;

	availableLanguages: {code: string; name: string; isDefault: boolean}[] = [];

	constructor(
		private languageService: LanguageService,
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
				name: this.getLanguageName(lang.languageCode),
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
			this.calculateModifiedFields();
		}
	}

	private calculateModifiedFields(): void {
		this.modifiedFields.clear();

		if(!this.draftWorkflowAction || !this.originalWorkflowAction) {
			return;
		}

		const draft = this.draftWorkflowAction;
		const original = this.originalWorkflowAction;

		if(draft.id !== original.id) {
			this.modifiedFields.add('id');
		}

		const translationFields: (keyof WorkflowAction)[] = ['shortname', 'longname', 'description'];

		translationFields.forEach(field => {
			const draftValue = draft[field] as Record<string, string> | undefined;
			const originalValue = original[field] as Record<string, string> | undefined;

			if(draftValue && originalValue) {
				const allLanguages = new Set([
					...Object.keys(draftValue),
					...Object.keys(originalValue)
				]);

				allLanguages.forEach(lang => {
					if(draftValue[lang] !== originalValue[lang]) {
						this.modifiedFields.add(`${field as string}.${lang}`);
					}
				});
			}
			else if(draftValue !== originalValue) {
				this.modifiedFields.add(field as string);
			}
		});
	}

	isFieldModified(fieldName: string): boolean {
		if(this.modifiedFields.has(fieldName)) {
			return true;
		}

		const languageFieldPattern = new RegExp(`^${fieldName}\\.`);
		return Array.from(this.modifiedFields).some(field => languageFieldPattern.test(field));
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

	onDelete(): void {
		if(!this.draftWorkflowAction) {
			return;
		}

		const workflowActionName = this.getTranslatedValue(this.draftWorkflowAction.shortname) || this.draftWorkflowAction.id;

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Workflow Action',
				message: `Are you sure you want to delete "${workflowActionName}"? This action cannot be undone.`,
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

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.selectedLanguage;
		return translations[lang] || '';
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
}
