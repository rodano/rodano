import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {ScopeModel} from '@core/model/scope-model';
import {ConfiguratorProject} from '@core/model/configurator-project';
import {Subscription} from 'rxjs';
import {
	WorkflowStateSelection
} from '../../dialogs/scope-model/scope-model-resources-dialog/scope-model-resources-dialog.component';
import {ScopeModelManagerService} from '../../services/manager/scope-model-manager.service';
import {LanguageService} from '../../services/language.service';
import {MatDialog} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ConfirmationDialogComponent} from '../../../confirmation-dialog/confirmation-dialog.component';
import {ScopeModelDialogService} from '../../services/dialogs/scope-model-dialog.service';
import {DatasetModelManagerService} from '../../services/manager/dataset-model-manager.service';
import {ProjectLanguage} from '@core/model/project-language';
import {WorkflowManagerService} from '../../services/manager/workflow-manager.service';
import {WorkflowStateManagerService} from '../../services/manager/workflow-state-manager.service';
import {ProfileManagerService} from '../../services/manager/profile-manager.service';
import {DangerZoneComponent} from '../../shared/danger-zone/danger-zone.component';

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
		MatTooltipModule,
		DangerZoneComponent
	]
})
export class ScopeModelDetailComponent implements OnInit, OnDestroy {
	@Input() scopeModel!: ScopeModel;
	@Input() projectId = '';
	@Input() project: ConfiguratorProject | null = null;
	@Input() allScopeModels: ScopeModel[] = [];
	@Output() scopeModelUpdated = new EventEmitter<ScopeModel>();
	@Output() scopeModelDeleted = new EventEmitter<string>();
	@Output() closed = new EventEmitter<void>();
	@Output() switchToEventModels = new EventEmitter<void>();
	@Output() switchToEventGroups = new EventEmitter<void>();

	selectedLanguage = '';
	projectLanguages: ProjectLanguage[] = [];
	private languageSubscription: Subscription;

	workflowStateIdsModified = false;

	constructor(
		public scopeModelManager: ScopeModelManagerService,
		public languageService: LanguageService,
		private scopeModelDialogService: ScopeModelDialogService,
		private datasetModelManager: DatasetModelManagerService,
		private workflowManager: WorkflowManagerService,
		private workflowStateManager: WorkflowStateManagerService,
		private profileManager: ProfileManagerService,
		private dialog: MatDialog,
		private snackBar: MatSnackBar
	) {}

	ngOnInit(): void {
		this.projectLanguages = this.project?.languages?.length ? this.project.languages : this.languageService.projectLanguages;
		this.languageSubscription = this.languageService.selectedLanguage$.subscribe(language => {
			this.selectedLanguage = language;
		});

		this.workflowStateManager.load(this.projectId).subscribe();
	}

	ngOnDestroy(): void {
		this.languageSubscription.unsubscribe();
	}

	onEditBasicInfo(): void {
		this.scopeModelDialogService.openBasicInfoDialog(
			this.projectId,
			this.scopeModel,
			this.projectLanguages
		).subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditRelationships(): void {
		this.scopeModelDialogService.openRelationshipsDialog(
			this.projectId,
			this.scopeModel
		).subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditDefaultSettings(): void {
		this.scopeModelDialogService.openDefaultSettingsDialog(
			this.projectId,
			this.scopeModel
		).subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditPattern(): void {
		this.scopeModelDialogService.openPatternDialog(
			this.scopeModel
		).subscribe((result: any) => {
			if(result) {
				const updatedScopeModel: ScopeModel = {...this.scopeModel, ...result};
				this.scopeModelManager.update(updatedScopeModel);
				this.scopeModelUpdated.emit(updatedScopeModel);
				this.showStagedMessage();
			}
		});
	}

	onEditResources(): void {
		const currentDraft = this.scopeModelManager.getById(this.scopeModel.scopeModelId);
		const draft = currentDraft || this.scopeModel;

		const workflowStateSelections: WorkflowStateSelection[] = (draft.workflowStateIds || []).map(stateId => {
			const state = this.workflowStateManager.getById(stateId);
			return {workflowId: state?.workflowId || '', workflowStateId: stateId};
		});

		this.scopeModelDialogService.openResourcesDialog(
			this.projectId,
			draft,
			workflowStateSelections
		).subscribe(result => {
			if(result) {
				const draft = this.scopeModelManager.getById(this.scopeModel.scopeModelId);
				if(draft) {
					Object.assign(draft, result);
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
				message: `Are you sure you want to delete "${this.languageService.getTranslatedName(this.scopeModel.shortname)}"?`,
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
		this.closed.emit();
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

	getWorkflowStateSelections(): WorkflowStateGroup[] {
		const grouped = new Map<string, {id: string; name: string}[]>();

		(this.scopeModel.workflowStateIds || []).forEach(stateId => {
			const state = this.workflowStateManager.getById(stateId);
			if(!state) {
				return;
			}
			if(!grouped.has(state.workflowId)) {
				grouped.set(state.workflowId, []);
			}
			const name = `${this.languageService.getDefaultTranslation(state.shortname) || state.id} (${state.id})`;
			grouped.get(state.workflowId)!.push({id: stateId, name});
		});

		const result: WorkflowStateGroup[] = [];
		grouped.forEach((states, workflowId) => result.push({
			workflowId,
			workflowName: this.getWorkflowLabel(workflowId),
			states
		}));
		return result;
	}

	getScopeModelLabel(scopeModelId: string): string {
		return this.languageService.getLabelById(scopeModelId, id => this.scopeModelManager.getById(id));
	}

	getDatasetModelLabel(datasetModelId: string): string {
		return this.languageService.getLabelById(datasetModelId, id => this.datasetModelManager.getById(id));
	}

	getFormModelLabel(formModelId: string): string {
		//TODO: Implement when form models are ready
		return formModelId;
	}

	getWorkflowLabel(workflowId: string): string {
		return this.languageService.getLabelById(workflowId, id => this.workflowManager.getById(id));
	}

	getProfileLabel(profileId: string): string {
		return this.languageService.getLabelById(profileId, id => this.profileManager.getById(id));
	}
}
