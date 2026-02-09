import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {
	ScopeModelResourcesDialogComponent,
	WorkflowStateSelection
} from '../scope-model-dialog/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {ScopeModelManagerService} from '../../services/scope-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
	ScopeModelBasicInfoDialogComponent
} from '../scope-model-dialog/scope-model-basic-info-dialog/scope-model-basic-info-dialog.component';
import {
	ScopeModelRelationshipsDialogComponent
} from '../scope-model-dialog/scope-model-relationships-dialog/scope-model-relationships-dialog.component';
import {
	ScopeModelDefaultSettingsDialogComponent
} from '../scope-model-dialog/scope-model-default-settings-dialog/scope-model-default-settings-dialog.component';
import {
	ScopeModelPatternDialogComponent
} from '../scope-model-dialog/scope-model-pattern-dialog/scope-model-pattern-dialog.component';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';

interface WorkflowStateGroup {
	workflowId: string;
	workflowName: string;
	states: {id: string; name: string}[];
}

@Component({
	selector: 'app-scope-model-detail',
	standalone: true,
	templateUrl: './scope-model-detail.component.html',
	styleUrls: ['./scope-model-detail.component.css'],
	imports: [
		CommonModule,
		MatIconModule,
		MatButtonModule,
		MatTooltipModule
	]
})
export class ScopeModelDetailComponent implements OnInit, OnDestroy {
	@Input() scopeModel!: ScopeModel;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allScopeModels: ScopeModel[] = [];
	@Output() scopeModelUpdated = new EventEmitter<ScopeModel>();
	@Output() scopeModelDeleted = new EventEmitter<string>();
	@Output() _close = new EventEmitter<void>();
	@Output() switchToEventModels = new EventEmitter<void>();
	@Output() switchToEventGroups = new EventEmitter<void>();

	selectedLanguage = '';
	private languageSubscription: Subscription;
	private workflowStateSelectionsMap = new Map<string, WorkflowStateSelection[]>();

	constructor(
		public scopeModelManager: ScopeModelManagerService,
		private languageService: LanguageService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		const dialogRef = this.dialog.open(ScopeModelBasicInfoDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(this.scopeModel)),
				languages: this.project?.languages || []
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditRelationships(): void {
		const dialogRef = this.dialog.open(ScopeModelRelationshipsDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(this.scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditDefaultSettings(): void {
		const dialogRef = this.dialog.open(ScopeModelDefaultSettingsDialogComponent, {
			width: '500px',
			data: {
				projectId: this.projectId,
				scopeModel: JSON.parse(JSON.stringify(this.scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditPattern(): void {
		const dialogRef = this.dialog.open(ScopeModelPatternDialogComponent, {
			width: '500px',
			data: {
				scopeModel: JSON.parse(JSON.stringify(this.scopeModel))
			}
		});

		dialogRef.afterClosed().subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditResources(): void {
		const dialogRef = this.dialog.open(ScopeModelResourcesDialogComponent, {
			data: {
				scopeModel: this.scopeModelManager.getById(this.scopeModel.scopeModelId),
				availableForms: [],
				availableDatasets: [],
				availableWorkflows: [],
				workflowStateSelections: this.workflowStateSelectionsMap.get(this.scopeModel.scopeModelId) || []
			},
			width: '500px',
			maxHeight: '90vh',
			disableClose: true
		});

		dialogRef.afterClosed().subscribe(result => {
			if(result) {
				const draft = this.scopeModelManager.getById(this.scopeModel.scopeModelId);
				if(draft) {
					Object.assign(draft, result);

					if(result.workflowStateSelections !== undefined) {
						this.workflowStateSelectionsMap.set(this.scopeModel.scopeModelId, result.workflowStateSelections);
					}

					this.scopeModelManager.update(draft);
					this.scopeModelUpdated.emit(draft);
					this.showStagedMessage();
				}
			}
		});
	}

	onDelete(): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			width: '500px',
			data: {
				title: 'Delete Scope Model',
				message: `Are you sure you want to delete "${this.getTranslatedName(this.scopeModel.shortname)}"?`,
				confirmText: 'Delete',
				cancelText: 'Cancel',
				type: 'danger'
			}
		});

		dialogRef.afterClosed().subscribe((confirmed: boolean) => {
			if(confirmed) {
				this.scopeModelDeleted.emit(this.scopeModel.scopeModelId);
			}
		});
	}

	onClose(): void {
		this._close.emit();
	}

	onSwitchToEventModels(): void {
		this.switchToEventModels.emit();
	}

	onSwitchToEventGroups(): void {
		this.switchToEventGroups.emit();
	}

	private showStagedMessage(): void {
		this.snackBar.open('Changes staged (not saved yet)', 'Close', {duration: 2000});
	}

	isFieldModified(fieldName: string): boolean {
		return this.scopeModelManager.isFieldModified(this.scopeModel.scopeModelId, fieldName);
	}

	getTranslatedName(translations: Record<string, string> | undefined): string {
		return this.getTranslatedValue(translations);
	}

	getTranslatedValue(translations: Record<string, string> | undefined, languageCode?: string): string {
		if(!translations) {
			return '';
		}
		const lang = languageCode || this.selectedLanguage;
		return translations[lang] || '';
	}

	getScopeModelLabel(scopeModelId: string): string {
		const scopeModel = this.allScopeModels.find(sm => sm.scopeModelId === scopeModelId);
		if(!scopeModel) {
			return scopeModelId;
		}

		const name = this.languageService.getDefaultTranslation(scopeModel.shortname) || scopeModel.id;
		return `${name} (${scopeModel.id})`;
	}

	getLanguageName(code: string | undefined): string {
		if(!code) {
			return 'Unknown';
		}
		try {
			const displayNames = new Intl.DisplayNames(['en'], {type: 'language'});
			return displayNames.of(code) || code.toUpperCase();
		}
		catch (e) {
			console.error(e);
			return code.toUpperCase();
		}
	}

	getWorkflowStateSelections(): WorkflowStateGroup[] {
		const selections = this.workflowStateSelectionsMap.get(this.scopeModel.scopeModelId) || [];

		const grouped = new Map<string, {id: string; name: string}[]>();

		selections.forEach(selection => {
			if(!grouped.has(selection.workflowId)) {
				grouped.set(selection.workflowId, []);
			}

			grouped.get(selection.workflowId)!.push({
				id: selection.workflowStateId,
				name: selection.workflowStateId
			});
		});

		const result: WorkflowStateGroup[] = [];
		grouped.forEach((states, workflowId) => {
			result.push({
				workflowId,
				workflowName: workflowId,
				states
			});
		});

		return result;
	}
}
